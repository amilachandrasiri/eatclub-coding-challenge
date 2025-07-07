package org.eatclub.codingchallenge.controller;

import org.eatclub.codingchallenge.model.response.PeakDealTimeRangeResponse;
import org.eatclub.codingchallenge.service.PeakDealTimeRangeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalTime;

import static org.mockito.Mockito.when;

@WebFluxTest(controllers = PeakDealTimeController.class)
@ActiveProfiles("test")
class PeakDealTimeControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private PeakDealTimeRangeService peakDealTimeRangeService;

    @Test
    void shouldReturnPeakTimeWindowSuccessfully() {
        // Given
        PeakDealTimeRangeResponse expectedResponse = PeakDealTimeRangeResponse.builder()
            .peakTimeStart(LocalTime.of(12, 0))
            .peakTimeEnd(LocalTime.of(15, 0))
            .build();

        when(peakDealTimeRangeService.getPeakDealTimeRange())
            .thenReturn(Mono.just(expectedResponse));

        // When
        webTestClient
            .get()
            .uri("/peak-time-window")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.peakTimeStart").isEqualTo("12:00:00")
            .jsonPath("$.peakTimeEnd").isEqualTo("15:00:00");
    }

    @Test
    void shouldHandleServiceExceptionThroughGlobalExceptionHandler() {
        // Given
        when(peakDealTimeRangeService.getPeakDealTimeRange())
            .thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        // When
        webTestClient
            .get()
            .uri("/peak-time-window")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().is5xxServerError()
            .expectBody(String.class)
            .isEqualTo("Internal Server error");
    }
}
