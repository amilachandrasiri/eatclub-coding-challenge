package org.eatclub.codingchallenge.service;

import org.eatclub.codingchallenge.model.Deals;
import org.eatclub.codingchallenge.model.Restaurant;
import org.eatclub.codingchallenge.model.RestaurantResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultPeakDealTimeRangeServiceTest {

    @Mock
    private RestaurantService restaurantService;

    private DefaultPeakDealTimeRangeService peakDealTimeRangeService;

    @BeforeEach
    void setUp() {
        peakDealTimeRangeService = new DefaultPeakDealTimeRangeService(restaurantService);
    }

    @Test
    void testGetPeakDealTimeRange_WithOverlappingDeals() {
        // Given
        Restaurant restaurant1 = createRestaurant("Restaurant1", "9:00am", "10:00pm",
            Arrays.asList(
                createDeal("10:00am", "2:00pm", null, null),
                createDeal("12:00pm", "4:00pm", null, null)
            ));

        Restaurant restaurant2 = createRestaurant("Restaurant2", "8:00am", "9:00pm",
            Collections.singletonList(
                createDeal("11:00am", "3:00pm", null, null)
            ));

        RestaurantResponse restaurantResponse = RestaurantResponse.builder()
            .restaurants(Arrays.asList(restaurant1, restaurant2))
            .build();

        when(restaurantService.getRestaurants()).thenReturn(Mono.just(restaurantResponse));

        // When
        StepVerifier.create(peakDealTimeRangeService.getPeakDealTimeRange())
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.getPeakTimeStart()).isEqualTo(LocalTime.of(12, 0));
                assertThat(response.getPeakTimeEnd()).isEqualTo(LocalTime.of(14, 0));
            })
            .verifyComplete();
    }

    @Test
    void testGetPeakDealTimeRange_WithDealOpenCloseFields() {
        // Given
        Restaurant restaurant = createRestaurant("Restaurant1", "9:00am", "10:00pm",
            Arrays.asList(
                createDeal(null, null, "10:00am", "2:00pm"),
                createDeal(null, null, "12:00pm", "4:00pm")
            ));

        RestaurantResponse restaurantResponse = RestaurantResponse.builder()
            .restaurants(Collections.singletonList(restaurant))
            .build();

        when(restaurantService.getRestaurants()).thenReturn(Mono.just(restaurantResponse));

        // When
        StepVerifier.create(peakDealTimeRangeService.getPeakDealTimeRange())
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.getPeakTimeStart()).isEqualTo(LocalTime.of(12, 0));
                assertThat(response.getPeakTimeEnd()).isEqualTo(LocalTime.of(14, 0));
            })
            .verifyComplete();
    }

    @Test
    void testGetPeakDealTimeRange_FallbackToRestaurantOpenTimes() {
        // Given
        Restaurant restaurant = createRestaurant("Restaurant1", "09:00am", "10:00pm",
            Arrays.asList(
                createDeal(null, null, null, null),
                createDeal(null, null, null, null)
            ));

        RestaurantResponse restaurantResponse = RestaurantResponse.builder()
            .restaurants(Collections.singletonList(restaurant))
            .build();

        when(restaurantService.getRestaurants()).thenReturn(Mono.just(restaurantResponse));

        // When
        StepVerifier.create(peakDealTimeRangeService.getPeakDealTimeRange())
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.getPeakTimeStart()).isEqualTo(LocalTime.of(9, 0));
                assertThat(response.getPeakTimeEnd()).isEqualTo(LocalTime.of(22, 0));
            })
            .verifyComplete();
    }

    @Test
    void testGetPeakDealTimeRange_PriorityStartTimeSelection() {
        // Given
        Restaurant restaurant = createRestaurant("Restaurant1", "09:00am", "10:00pm",
            Collections.singletonList(
                createDeal("10:00am", "2:00pm", "11:00am", "1:00pm")
            ));

        RestaurantResponse restaurantResponse = RestaurantResponse.builder()
            .restaurants(Collections.singletonList(restaurant))
            .build();

        when(restaurantService.getRestaurants()).thenReturn(Mono.just(restaurantResponse));

        // When
        StepVerifier.create(peakDealTimeRangeService.getPeakDealTimeRange())
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.getPeakTimeStart()).isEqualTo(LocalTime.of(10, 0));
                assertThat(response.getPeakTimeEnd()).isEqualTo(LocalTime.of(14, 0));
            })
            .verifyComplete();
    }

    @Test
    void testGetPeakDealTimeRange_WithEmptyRestaurants() {
        // Given
        RestaurantResponse restaurantResponse = RestaurantResponse.builder()
            .restaurants(Collections.emptyList())
            .build();

        when(restaurantService.getRestaurants()).thenReturn(Mono.just(restaurantResponse));

        // When
        StepVerifier.create(peakDealTimeRangeService.getPeakDealTimeRange())
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.getPeakTimeStart()).isNull();
                assertThat(response.getPeakTimeEnd()).isNull();
            })
            .verifyComplete();
    }

    @Test
    void testGetPeakDealTimeRange_WithRestaurantWithoutDeals() {
        // Given
        Restaurant restaurant = createRestaurant("Restaurant1", "09:00", "22:00", Collections.emptyList());

        RestaurantResponse restaurantResponse = RestaurantResponse.builder()
            .restaurants(Collections.singletonList(restaurant))
            .build();

        when(restaurantService.getRestaurants()).thenReturn(Mono.just(restaurantResponse));

        // When
        StepVerifier.create(peakDealTimeRangeService.getPeakDealTimeRange())
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.getPeakTimeStart()).isNull();
                assertThat(response.getPeakTimeEnd()).isNull();
            })
            .verifyComplete();
    }

    @Test
    void testGetPeakDealTimeRange_WithError() {
        // Given
        when(restaurantService.getRestaurants()).thenReturn(Mono.error(new RuntimeException("Service error")));

        // When
        StepVerifier.create(peakDealTimeRangeService.getPeakDealTimeRange())
            .expectError(RuntimeException.class)
            .verify();
    }

    @Test
    void testDealStatusChangeEvent_Comparison() {
        // Given
        DefaultPeakDealTimeRangeService.DealStatusChangeEvent startEvent =
            new DefaultPeakDealTimeRangeService.DealStatusChangeEvent(LocalTime.of(10, 0), true);
        DefaultPeakDealTimeRangeService.DealStatusChangeEvent endEvent =
            new DefaultPeakDealTimeRangeService.DealStatusChangeEvent(LocalTime.of(10, 0), false);
        DefaultPeakDealTimeRangeService.DealStatusChangeEvent laterEvent =
            new DefaultPeakDealTimeRangeService.DealStatusChangeEvent(LocalTime.of(11, 0), true);

        // When
        assertThat(startEvent.compareTo(endEvent)).isLessThan(0);
        assertThat(startEvent.compareTo(laterEvent)).isLessThan(0);
        assertThat(laterEvent.compareTo(startEvent)).isGreaterThan(0);
    }

    @Test
    void testGetPeakDealTimeRange_SingleDeal() {
        // Given
        Restaurant restaurant = createRestaurant("Restaurant1", "09:00", "10:00pm",
            Collections.singletonList(
                createDeal("10:00am", "2:00pm", null, null)
            ));

        RestaurantResponse restaurantResponse = RestaurantResponse.builder()
            .restaurants(Collections.singletonList(restaurant))
            .build();

        when(restaurantService.getRestaurants()).thenReturn(Mono.just(restaurantResponse));

        // When
        StepVerifier.create(peakDealTimeRangeService.getPeakDealTimeRange())
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.getPeakTimeStart()).isEqualTo(LocalTime.of(10, 0));
                assertThat(response.getPeakTimeEnd()).isEqualTo(LocalTime.of(14, 0));
            })
            .verifyComplete();
    }

    @Test
    void testGetPeakDealTimeRange_ComplexOverlapScenario() {
        // Given
        Restaurant restaurant1 = createRestaurant("Restaurant1", "9:00am", "10:00pm",
            Arrays.asList(
                createDeal("9:00am", "3:00pm", null, null),
                createDeal("10:00am", "2:00pm", null, null),
                createDeal("11:00am", "1:00pm", null, null),
                createDeal("12:00pm", "4:00pm", null, null)
            ));

        RestaurantResponse restaurantResponse = RestaurantResponse.builder()
            .restaurants(Collections.singletonList(restaurant1))
            .build();

        when(restaurantService.getRestaurants()).thenReturn(Mono.just(restaurantResponse));

        // When
        StepVerifier.create(peakDealTimeRangeService.getPeakDealTimeRange())
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.getPeakTimeStart()).isEqualTo(LocalTime.of(12, 0));
                assertThat(response.getPeakTimeEnd()).isEqualTo(LocalTime.of(13, 0));
            })
            .verifyComplete();
    }

    @Test
    void testGetPeakDealTimeRange_MultipleMaxRanges_FirstPeak() {
        // Given
        Restaurant restaurant = createRestaurant("Restaurant1", "9:00am", "10:00pm",
            Arrays.asList(
                createDeal("9:00am", "11:00am", null, null),
                createDeal("10:00am", "12:00pm", null, null),
                createDeal("2:00pm", "4:00pm", null, null),
                createDeal("3:00pm", "5:00pm", null, null)
            ));

        RestaurantResponse restaurantResponse = RestaurantResponse.builder()
            .restaurants(Collections.singletonList(restaurant))
            .build();

        when(restaurantService.getRestaurants()).thenReturn(Mono.just(restaurantResponse));

        // When
        StepVerifier.create(peakDealTimeRangeService.getPeakDealTimeRange())
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.getPeakTimeStart()).isEqualTo(LocalTime.of(10, 0));
                assertThat(response.getPeakTimeEnd()).isEqualTo(LocalTime.of(11, 0));
            })
            .verifyComplete();
    }

    @Test
    void testGetPeakDealTimeRange_DealEndPrioritySelection() {
        // Given
        Restaurant restaurant = createRestaurant("Restaurant1", "9:00am", "10:00pm",
            Collections.singletonList(
                createDeal("10:00am", "2:00pm", "11:00am", "1:00pm")
            ));

        RestaurantResponse restaurantResponse = RestaurantResponse.builder()
            .restaurants(Collections.singletonList(restaurant))
            .build();

        when(restaurantService.getRestaurants()).thenReturn(Mono.just(restaurantResponse));

        // When
        StepVerifier.create(peakDealTimeRangeService.getPeakDealTimeRange())
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.getPeakTimeStart()).isEqualTo(LocalTime.of(10, 0));
                assertThat(response.getPeakTimeEnd()).isEqualTo(LocalTime.of(14, 0));
            })
            .verifyComplete();
    }

    private Restaurant createRestaurant(String name, String open, String close, List<Deals> deals) {
        return Restaurant.builder()
            .name(name)
            .open(open)
            .close(close)
            .deals(deals)
            .build();
    }

    private Deals createDeal(String start, String end, String open, String close) {
        return Deals.builder()
            .start(start)
            .end(end)
            .open(open)
            .close(close)
            .qtyLeft(10)
            .build();
    }
}
