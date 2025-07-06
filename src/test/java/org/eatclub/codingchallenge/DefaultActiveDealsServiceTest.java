package org.eatclub.codingchallenge;

import org.eatclub.codingchallenge.model.Deals;
import org.eatclub.codingchallenge.model.Restaurant;
import org.eatclub.codingchallenge.model.RestaurantResponse;
import org.eatclub.codingchallenge.model.response.ActiveDealsResponse;
import org.eatclub.codingchallenge.model.response.DealsItem;
import org.eatclub.codingchallenge.service.DefaultActiveDealsService;
import org.eatclub.codingchallenge.util.ActiveDealsResponseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class DefaultActiveDealsServiceTest {
    final static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm[a]");

    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;
    @Spy
    private ActiveDealsResponseMapper activeDealsResponseMapper;
    private DefaultActiveDealsService activeDealsService;

    @BeforeEach
    void setUp() {
        activeDealsService = new DefaultActiveDealsService(webClient, activeDealsResponseMapper);

        // Set private fields using reflection
        setField(activeDealsService, "restaurantsUrl", "http://localhost:8080/restaurants");
        setField(activeDealsService, "cacheTTL", 60);

        // Setup WebClient mock chain
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(ArgumentMatchers.anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void shouldReturnEmptyDealsWhenRestaurantIsClosed() {
        
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
        activeDealsService.initCache();

        // When
        Mono<ActiveDealsResponse> result = activeDealsService.getActiveDealsAt(LocalTime.parse("10:00pm", TIME_FORMATTER));

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.getDeals()).isEmpty();
            })
            .verifyComplete();
    }

    @Test
    void shouldReturnEmptyDealsWhenQtyLeftIsZero() {
        // Given
        Restaurant restaurant = createRestaurant("3:00pm", "9:00pm",
            createDeal("3:00pm", "9:00pm", 0L));

        RestaurantResponse restaurantResponse = RestaurantResponse.builder()
            .restaurants(List.of(restaurant))
            .build();

        when(responseSpec.onStatus(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(responseSpec);
        when(responseSpec.bodyToMono(RestaurantResponse.class))
            .thenReturn(Mono.just(restaurantResponse));
        // Simulate @PostConstruct
        activeDealsService.initCache();

        // When
        Mono<ActiveDealsResponse> result = activeDealsService.getActiveDealsAt(LocalTime.parse("8:00pm", TIME_FORMATTER));

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.getDeals()).isEmpty();
            })
            .verifyComplete();
    }

    @Test
    void shouldReturnActiveDealsWhenRestaurantIsOpenAndDealsAreActive() {
        // Given
        Restaurant restaurant = createRestaurant("3:00pm", "9:00pm",
            createDeal("4:00pm", "6:00pm", 10L));

        RestaurantResponse restaurantResponse = RestaurantResponse.builder()
            .restaurants(List.of(restaurant))
            .build();

        DealsItem expectedDealsItem = createDealsItem("3:00pm", "9:00pm", 10L);

        when(responseSpec.onStatus(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(responseSpec);
        when(responseSpec.bodyToMono(RestaurantResponse.class))
            .thenReturn(Mono.just(restaurantResponse));
        doCallRealMethod().when(activeDealsResponseMapper).mapToDealsItem(ArgumentMatchers.any(), ArgumentMatchers.any());
        // Simulate @PostConstruct
        activeDealsService.initCache();

        // When
        Mono<ActiveDealsResponse> result = activeDealsService.getActiveDealsAt(LocalTime.parse("5:00pm", TIME_FORMATTER));

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.getDeals()).hasSize(1);
                assertThat(response.getDeals().getFirst()).isEqualTo(expectedDealsItem);
            })
            .verifyComplete();

        verify(activeDealsResponseMapper).mapToDealsItem(ArgumentMatchers.eq(restaurant), ArgumentMatchers.any(Deals.class));
    }

    @Test
    void shouldFilterDealsBasedOnDealStartEndTimes() {
        // Given
        Restaurant restaurant = createRestaurant("1:00pm", "11:00pm",
            createDealWithStartEnd("2:00pm", "5:00pm", 3L),
            createDealWithStartEnd("6:00pm", "9:00pm", 4L));

        RestaurantResponse restaurantResponse = RestaurantResponse.builder()
            .restaurants(List.of(restaurant))
            .build();

        DealsItem expectedDealsItem = createDealsItem("1:00pm", "11:00pm", 4L);

        when(responseSpec.onStatus(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(responseSpec);
        when(responseSpec.bodyToMono(RestaurantResponse.class))
            .thenReturn(Mono.just(restaurantResponse));
        // Simulate @PostConstruct
        activeDealsService.initCache();

        // When - requesting deals at 3:00pm (only first deal should be active)
        Mono<ActiveDealsResponse> result = activeDealsService.getActiveDealsAt(LocalTime.parse("7:00pm", TIME_FORMATTER));
        doCallRealMethod().when(activeDealsResponseMapper).mapToDealsItem(ArgumentMatchers.any(), ArgumentMatchers.any());

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.getDeals()).hasSize(1);
                assertThat(response.getDeals().getFirst()).isEqualTo(expectedDealsItem);
            })
            .verifyComplete();
    }


    @Test
    void shouldReturnEmptyListWhenEmptyRestaurantsList() {
        // Given
        RestaurantResponse restaurantResponse = RestaurantResponse.builder()
            .restaurants(Collections.emptyList())
            .build();

        when(responseSpec.onStatus(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(responseSpec);
        when(responseSpec.bodyToMono(RestaurantResponse.class))
            .thenReturn(Mono.just(restaurantResponse));
        // Simulate @PostConstruct
        activeDealsService.initCache();

        // When
        Mono<ActiveDealsResponse> result = activeDealsService.getActiveDealsAt(LocalTime.parse("4:00pm", TIME_FORMATTER));

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.getDeals()).isEmpty();
            })
            .verifyComplete();
    }

    @Test
    void shouldReturnEmptyListWhenNoDealsInRestaurant() {
        // Given
        Restaurant restaurant = Restaurant.builder()
            .open("3:00pm")
            .close("9:00pm")
            .deals(Collections.emptyList())
            .build();

        RestaurantResponse restaurantResponse = RestaurantResponse.builder()
            .restaurants(List.of(restaurant))
            .build();

        when(responseSpec.onStatus(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(responseSpec);
        when(responseSpec.bodyToMono(RestaurantResponse.class))
            .thenReturn(Mono.just(restaurantResponse));
        // Simulate @PostConstruct
        activeDealsService.initCache();

        // When
        Mono<ActiveDealsResponse> result = activeDealsService.getActiveDealsAt(LocalTime.parse("4:00pm"));

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.getDeals()).isEmpty();
            })
            .verifyComplete();

    }

    @Test
    void shouldHandleMultipleRestaurantsWithMixedActiveDeals() {
        // Given
        Restaurant restaurant1 = createRestaurant("3:00pm", "9:00pm",
            createDeal("3:00pm", "9:00pm", 5L));

        Restaurant restaurant2 = createRestaurant("12:00pm", "11:00pm",
            createDeal("12:00pm", "11:00pm", 3L));

        Restaurant restaurant3 = createRestaurant("6:00pm", "10:00pm",
            createDeal("6:00pm", "10:00pm", 0L));

        RestaurantResponse restaurantResponse = RestaurantResponse.builder()
            .restaurants(Arrays.asList(restaurant1, restaurant2, restaurant3))
            .build();

        when(responseSpec.onStatus(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(responseSpec);
        when(responseSpec.bodyToMono(RestaurantResponse.class))
            .thenReturn(Mono.just(restaurantResponse));
        doCallRealMethod().when(activeDealsResponseMapper).mapToDealsItem(ArgumentMatchers.any(), ArgumentMatchers.any());

        // Simulate @PostConstruct
        activeDealsService.initCache();

        // When
        Mono<ActiveDealsResponse> result = activeDealsService.getActiveDealsAt(LocalTime.parse("4:00pm", TIME_FORMATTER));

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.getDeals()).hasSize(2);
            })
            .verifyComplete();
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
        doCallRealMethod().when(activeDealsResponseMapper).mapToDealsItem(ArgumentMatchers.any(), ArgumentMatchers.any());

        // Simulate @PostConstruct
        activeDealsService.initCache();

        // When - call twice in a row under TTL
        Mono<ActiveDealsResponse> result1 = activeDealsService.getActiveDealsAt(LocalTime.parse("4:00pm", TIME_FORMATTER));
        Mono<ActiveDealsResponse> result2 = activeDealsService.getActiveDealsAt(LocalTime.parse("4:00pm", TIME_FORMATTER));

        // Then
        StepVerifier.create(result1)
            .assertNext(response -> assertThat(response.getDeals()).hasSize(1))
            .verifyComplete();

        StepVerifier.create(result2)
            .assertNext(response -> assertThat(response.getDeals()).hasSize(1))
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

        // When
        activeDealsService.initCache();
        Mono<ActiveDealsResponse> result = activeDealsService.getActiveDealsAt(LocalTime.parse("4:00pm", TIME_FORMATTER));

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

        // When
        activeDealsService.initCache();
        Mono<ActiveDealsResponse> result = activeDealsService.getActiveDealsAt(LocalTime.parse("4:00pm", TIME_FORMATTER));

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

    private Deals createDealWithStartEnd(String start, String end, Long qtyLeft) {
        return Deals.builder()
            .start(start)
            .end(end)
            .qtyLeft(qtyLeft)
            .build();
    }

    private DealsItem createDealsItem(String restaurantOpen, String restaurantClose, long qtyLeft) {
        return DealsItem.builder()
            .restaurantOpen(restaurantOpen)
            .restaurantClose(restaurantClose)
            .qtyLeft(qtyLeft)
            .build();
    }
}