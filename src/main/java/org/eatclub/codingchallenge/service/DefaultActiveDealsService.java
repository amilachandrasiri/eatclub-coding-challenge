package org.eatclub.codingchallenge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eatclub.codingchallenge.model.Deals;
import org.eatclub.codingchallenge.model.Restaurant;
import org.eatclub.codingchallenge.model.response.ActiveDealsResponse;
import org.eatclub.codingchallenge.model.response.DealsItem;
import org.eatclub.codingchallenge.util.ActiveDealsResponseMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Mono;

import java.time.LocalTime;
import java.util.List;

import static org.eatclub.codingchallenge.util.Constants.TIME_FORMATTER;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultActiveDealsService implements ActiveDealsService {

    private final RestaurantService restaurantService;
    private final ActiveDealsResponseMapper activeDealsResponseMapper;

    @Override
    public Mono<ActiveDealsResponse> getActiveDealsAt(LocalTime timeOfDay) {

        return restaurantService.getRestaurants().map(restaurantResponse -> {
            final List<DealsItem> dealsItemList = restaurantResponse.getRestaurants()
                .stream()
                .flatMap(restaurant -> restaurant.getDeals()
                    .stream()
                    .filter(deal -> isDealActive(restaurant, deal, timeOfDay))
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
