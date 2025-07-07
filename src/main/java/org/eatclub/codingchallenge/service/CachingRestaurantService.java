package org.eatclub.codingchallenge.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eatclub.codingchallenge.model.RestaurantResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RequiredArgsConstructor
@Slf4j
@Service
public class CachingRestaurantService implements RestaurantService {

    private final WebClient webClient;
    @Value("${upstream.restaurants.url}")
    private String restaurantsUrl;
    @Value("${upstream.restaurants.cacheTTL}")
    private int cacheTTL;
    private Mono<RestaurantResponse> cachedRestaurantResponse;

    // Cache the initial response mono for reuse until cache expires
    @PostConstruct
    public void initCache() {
        this.cachedRestaurantResponse = webClient
            .get()
            .uri(restaurantsUrl)
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                response -> Mono.error(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error")))
            .bodyToMono(RestaurantResponse.class)
            .cache(Duration.ofSeconds(cacheTTL))
            .doOnNext(response -> log.debug("Fetched {} restaurants from upstream service", response.getRestaurants().size()));
    }

    public Mono<RestaurantResponse> getRestaurants() {
        return cachedRestaurantResponse;
    }

}
