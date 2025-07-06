package org.eatclub.codingchallenge.service;

import reactor.core.publisher.Mono;

public interface ActiveDealsService {
    Mono<String> getActiveDeals(String timeOfDay);
}
