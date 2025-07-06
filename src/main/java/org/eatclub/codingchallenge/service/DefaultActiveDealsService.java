package org.eatclub.codingchallenge.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DefaultActiveDealsService implements ActiveDealsService {
    @Override
    public Mono<String> getActiveDeals(String timeOfDay) {
        return Mono.just("Active deals data   !!");
    }
}
