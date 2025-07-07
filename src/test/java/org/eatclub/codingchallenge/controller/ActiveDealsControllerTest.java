package org.eatclub.codingchallenge.controller;

import org.eatclub.codingchallenge.model.response.ActiveDealsResponse;
import org.eatclub.codingchallenge.model.response.DealsItem;
import org.eatclub.codingchallenge.service.ActiveDealsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = ActiveDealsController.class)
@ActiveProfiles("test")
class ActiveDealsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ActiveDealsService activeDealsService;

    @Test
    void shouldReturnActiveDealsSuccessfully() {
        // Given
        DealsItem dealsItem = DealsItem.builder()
            .restaurantObjectId("rest123")
            .restaurantName("Test Restaurant")
            .restaurantAddress1("123 Main St")
            .restarantSuburb("Test Suburb")
            .restaurantOpen("3:00pm")
            .restaurantClose("9:00pm")
            .dealObjectId("deal456")
            .discount("20%")
            .dineIn(true)
            .lightning(false)
            .qtyLeft(5L)
            .build();

        ActiveDealsResponse expectedResponse = ActiveDealsResponse.builder()
            .deals(List.of(dealsItem))
            .build();

        when(activeDealsService.getActiveDealsAt(any(LocalTime.class)))
            .thenReturn(Mono.just(expectedResponse));

        // When
        webTestClient
            .get()
            .uri("/active-deals?timeOfDay=4:00pm")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.deals").isArray()
            .jsonPath("$.deals[0].restaurantObjectId").isEqualTo("rest123")
            .jsonPath("$.deals[0].restaurantName").isEqualTo("Test Restaurant")
            .jsonPath("$.deals[0].restaurantAddress1").isEqualTo("123 Main St")
            .jsonPath("$.deals[0].restarantSuburb").isEqualTo("Test Suburb")
            .jsonPath("$.deals[0].restaurantOpen").isEqualTo("3:00pm")
            .jsonPath("$.deals[0].restaurantClose").isEqualTo("9:00pm")
            .jsonPath("$.deals[0].dealObjectId").isEqualTo("deal456")
            .jsonPath("$.deals[0].discount").isEqualTo("20%")
            .jsonPath("$.deals[0].dineIn").isEqualTo(true)
            .jsonPath("$.deals[0].lightning").isEqualTo(false)
            .jsonPath("$.deals[0].qtyLeft").isEqualTo(5);
    }

    @Test
    void shouldReturnBadRequestWhenTimeOfDayParameterMissing() {
        webTestClient
            .get()
            .uri("/active-deals")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnBadRequestWhenTimeOfDayParameterEmpty() {
        webTestClient
            .get()
            .uri("/active-deals?timeOfDay=")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnBadRequestWhenTimeOfDayParameterIsBlank() {
        webTestClient
            .get()
            .uri("/active-deals?timeOfDay=   ")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnBadRequestWhenTimeOfDayFormatIsInvalid() {
        webTestClient
            .get()
            .uri("/active-deals?timeOfDay=invalid-time")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void shouldHandleServiceExceptionThroughGlobalExceptionHandler() {
        // Given
        when(activeDealsService.getActiveDealsAt(any(LocalTime.class)))
            .thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        // Then
        webTestClient
            .get()
            .uri("/active-deals?timeOfDay=4:00pm")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().is5xxServerError();
    }

    @Test
    void shouldAcceptValidTimeFormats() {
        // Given
        ActiveDealsResponse expectedResponse = ActiveDealsResponse.builder()
            .deals(Collections.emptyList())
            .build();

        when(activeDealsService.getActiveDealsAt(any(LocalTime.class)))
            .thenReturn(Mono.just(expectedResponse));

        // When
        webTestClient
            .get()
            .uri("/active-deals?timeOfDay=4:00pm")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk();

        webTestClient
            .get()
            .uri("/active-deals?timeOfDay=16:00")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk();

        webTestClient
            .get()
            .uri("/active-deals?timeOfDay=04:00")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk();
    }
}