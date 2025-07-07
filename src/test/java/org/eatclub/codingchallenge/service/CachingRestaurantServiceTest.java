package org.eatclub.codingchallenge.service;

import org.eatclub.codingchallenge.model.Deals;
import org.eatclub.codingchallenge.model.Restaurant;
import org.eatclub.codingchallenge.model.RestaurantResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class CachingRestaurantServiceTest {
    
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;
    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
        restaurantService = new CachingRestaurantService(webClient);

        // Set private fields using reflection
        setField(restaurantService, "restaurantsUrl", "http://localhost:8080/restaurants");
        setField(restaurantService, "cacheTTL", 60);

        // Setup WebClient mock chain
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(ArgumentMatchers.anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }



    @Test
    void shouldUseCacheForConsecutiveCalls() {
        // Given
        Restaurant restaurant = createRestaurant("3:00pm", "9:00pm",
            createDeal("3:00pm", "9:00pm", 5L));

        RestaurantResponse restaurantResponse = RestaurantResponse.builder()
            .restaurants(List.of(restaurant))
            .build();

        when(responseSpec.onStatus(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(responseSpec);
        when(responseSpec.bodyToMono(RestaurantResponse.class))
            .thenReturn(Mono.just(restaurantResponse));

        // Simulate @PostConstruct
        ReflectionTestUtils.invokeMethod(restaurantService, "initCache");

        // When - call twice in a row under TTL
        Mono<RestaurantResponse> result1 = restaurantService.getRestaurants();
        Mono<RestaurantResponse> result2 = restaurantService.getRestaurants();

        // Then
        StepVerifier.create(result1)
            .assertNext(response -> assertThat(response.getRestaurants()).hasSize(1))
            .verifyComplete();

        StepVerifier.create(result2)
            .assertNext(response -> assertThat(response.getRestaurants()).hasSize(1))
            .verifyComplete();

        verify(webClient, times(1)).get();
    }

    @Test
    void shouldHandleServerErrorResponses() {

        // Mock 5xx error response
        when(responseSpec.onStatus(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(responseSpec);
        when(responseSpec.bodyToMono(RestaurantResponse.class))
            .thenReturn(Mono.error(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error")));

        // Simulate @PostConstruct
        ReflectionTestUtils.invokeMethod(restaurantService, "initCache");

        // When
        Mono<RestaurantResponse> result = restaurantService.getRestaurants();

        // Then
        StepVerifier.create(result)
            .expectErrorMatches(throwable ->
                throwable instanceof HttpServerErrorException &&
                    ((HttpServerErrorException) throwable).getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR))
            .verify();
    }

    @Test
    void shouldHandleClientAndServerErrorResponses() {

        // Mock 5xx error response
        when(responseSpec.onStatus(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(responseSpec);
        when(responseSpec.bodyToMono(RestaurantResponse.class))
            .thenReturn(Mono.error(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error")));

        // Simulate @PostConstruct
        ReflectionTestUtils.invokeMethod(restaurantService, "initCache");

        // When
        Mono<RestaurantResponse> result = restaurantService.getRestaurants();

        // Then
        StepVerifier.create(result)
            .expectErrorMatches(throwable ->
                throwable instanceof HttpServerErrorException &&
                    ((HttpServerErrorException) throwable).getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR))
            .verify();
    }


    private Restaurant createRestaurant(String open, String close, Deals... deals) {
        return Restaurant.builder()
            .open(open)
            .close(close)
            .deals(Arrays.asList(deals))
            .build();
    }

    private Deals createDeal(String open, String close, Long qtyLeft) {
        return Deals.builder()
            .open(open)
            .close(close)
            .qtyLeft(qtyLeft)
            .build();
    }
}