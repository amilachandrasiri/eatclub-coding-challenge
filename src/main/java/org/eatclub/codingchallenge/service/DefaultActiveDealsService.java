package org.eatclub.codingchallenge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eatclub.codingchallenge.model.Deals;
import org.eatclub.codingchallenge.model.Restaurant;
import org.eatclub.codingchallenge.model.RestaurantResponse;
import org.eatclub.codingchallenge.model.response.ActiveDealsResponse;
import org.eatclub.codingchallenge.model.response.DealsItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultActiveDealsService implements ActiveDealsService {

  final static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mma");
  private final WebClient webClient;

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
        .map(restaurantResponse -> restaurantResponse.getRestaurants()
          .stream()
          .flatMap(restaurant -> restaurant.getDeals()
            .stream()
            .filter(deal -> isDealActive(restaurant, deal, timeOfDayLocalTime))
            .map(activeDeal -> mapToDealsItem(restaurant, activeDeal))).toList())
        .flatMap(dealsList -> Mono.just(ActiveDealsResponse.builder().deals(dealsList).build()));
  }

  private boolean isDealActive(
      final Restaurant restaurant, final Deals deal, final LocalTime timeOfDayLocalTime) {
    final LocalTime restaurantOpen = LocalTime.parse(restaurant.getOpen(), TIME_FORMATTER);
    final LocalTime restaurantClose = LocalTime.parse(restaurant.getClose(), TIME_FORMATTER);

    if (restaurantOpen.isAfter(timeOfDayLocalTime)
        && restaurantClose.isBefore(timeOfDayLocalTime)) {
      return false;
    }

    if (deal.getQtyLeft() <= 0) {
      return false;
    }

    LocalTime actualDealStartTime =
        !ObjectUtils.isEmpty(deal.getStart())
            ? LocalTime.parse(deal.getStart(), TIME_FORMATTER)
            : !ObjectUtils.isEmpty(deal.getOpen())
                ? LocalTime.parse(deal.getOpen(), TIME_FORMATTER)
                : restaurantOpen;

    LocalTime actualDealEndTime =
        !ObjectUtils.isEmpty(deal.getEnd())
            ? LocalTime.parse(deal.getEnd(), TIME_FORMATTER)
            : !ObjectUtils.isEmpty(deal.getClose())
                ? LocalTime.parse(deal.getClose(), TIME_FORMATTER)
                : restaurantClose;

    return actualDealStartTime.isAfter(timeOfDayLocalTime)
        && actualDealEndTime.isBefore(timeOfDayLocalTime);
  }

  private DealsItem mapToDealsItem(final Restaurant restaurant, final Deals deal) {
    return DealsItem.builder()
        .restaurantObjectId(restaurant.getObjectId())
        .restaurantName(restaurant.getName())
        .restaurantAddress1(restaurant.getAddress1())
        .restarantSuburb(restaurant.getSuburb())
        .restaurantOpen(restaurant.getOpen())
        .restaurantClose(restaurant.getClose())
        .dealObjectId(deal.getObjectId())
        .discount(deal.getDiscount())
        .dineIn(deal.isDineIn())
        .lightning(deal.isLightning())
        .qtyLeft(deal.getQtyLeft())
        .build();
  }
}
