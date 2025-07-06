package org.eatclub.codingchallenge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eatclub.codingchallenge.model.Deals;
import org.eatclub.codingchallenge.model.Restaurant;
import org.eatclub.codingchallenge.model.RestaurantResponse;
import org.eatclub.codingchallenge.model.response.ActiveDealsResponse;
import org.eatclub.codingchallenge.model.response.DealsItem;
import org.eatclub.codingchallenge.util.ActiveDealsResponseMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultActiveDealsService implements ActiveDealsService {

    final static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm[a]");

    private final WebClient webClient;
    private final ActiveDealsResponseMapper activeDealsResponseMapper;
    @Value("${upstream.restaurants.url}")
    private String restaurantsUrl;
    @Value("${upstream.restaurants.cacheTTL}")
    private int cacheTTL;

    @Override
    public Mono<ActiveDealsResponse> getActiveDealsAt(String timeOfDay) {

        final LocalTime timeOfDayLocalTime = LocalTime.parse(timeOfDay, TIME_FORMATTER);

        return webClient
            .get()
            .uri(restaurantsUrl)
            .retrieve()
            .bodyToMono(RestaurantResponse.class)
            .cache(Duration.ofSeconds(cacheTTL))
            .map(restaurantResponse -> {
                final List<DealsItem> dealsItemList = restaurantResponse.getRestaurants()
                    .stream()
                    .flatMap(restaurant -> restaurant.getDeals()
                        .stream()
                        .filter(deal -> isDealActive(restaurant, deal, timeOfDayLocalTime))
                        .map(activeDeal -> this.activeDealsResponseMapper.mapToDealsItem(restaurant, activeDeal)))
                    .toList();
                return ActiveDealsResponse.builder().deals(dealsItemList).build();
            });
    }

    private boolean isDealActive(
        final Restaurant restaurant, final Deals deal, final LocalTime timeOfDayLocalTime) {
        final LocalTime restaurantOpen = LocalTime.parse(restaurant.getOpen(), TIME_FORMATTER);
        final LocalTime restaurantClose = LocalTime.parse(restaurant.getClose(), TIME_FORMATTER);

        // Deal not active if the restaurant is closed at the requested time, exclude early
        if (restaurantOpen.isAfter(timeOfDayLocalTime)
            && restaurantClose.isBefore(timeOfDayLocalTime)) {
            return false;
        }

        // No deals left, exclude early
        if (deal.getQtyLeft() <= 0) {
            return false;
        }

        // Actual start time and end times are calculated with following order of priority
        // deal start -> deal open -> restaurant open
        // deal end -> deal close -> restaurant close
        LocalTime actualDealStartTime =
            !ObjectUtils.isEmpty(deal.getStart())
                ? LocalTime.parse(deal.getStart(), TIME_FORMATTER)
                : !ObjectUtils.isEmpty(deal.getOpen())
                ? LocalTime.parse(deal.getOpen(), TIME_FORMATTER)
                : restaurantOpen;

        LocalTime actualDealEndTime =
            !ObjectUtils.isEmpty(deal.getEnd()) ?
                LocalTime.parse(deal.getEnd(), TIME_FORMATTER)
                : !ObjectUtils.isEmpty(deal.getClose())
                ? LocalTime.parse(deal.getClose(), TIME_FORMATTER)
                : restaurantClose;

        return timeOfDayLocalTime.isAfter(actualDealStartTime)
            && timeOfDayLocalTime.isBefore(actualDealEndTime);
    }
}
