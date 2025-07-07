package org.eatclub.codingchallenge.service;

import org.eatclub.codingchallenge.model.RestaurantResponse;
import reactor.core.publisher.Mono;

public interface RestaurantService {
    Mono<RestaurantResponse> getRestaurants();
}
