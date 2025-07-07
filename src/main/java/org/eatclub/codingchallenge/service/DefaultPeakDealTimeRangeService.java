package org.eatclub.codingchallenge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eatclub.codingchallenge.model.response.PeakDealTimeRangeResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Mono;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.eatclub.codingchallenge.util.Constants.TIME_FORMATTER;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultPeakDealTimeRangeService implements PeakDealTimeRangeService {

    private final RestaurantService restaurantService;

    @Override
    public Mono<PeakDealTimeRangeResponse> getPeakDealTimeRange() {
        return restaurantService.getRestaurants()
            .map(restaurantResponse -> {
                List<DealStatusChangeEvent> allDealStartEndTimes = restaurantResponse.getRestaurants()
                    .stream()
                    .flatMap(restaurant -> restaurant.getDeals().stream()
                        .map(deal -> {
                            // Only care about deal start and end times, ignore other attributes and create the deal
                            // status change event list in a single loop.
                            // Actual start time and end times are calculated with following order of priority
                            // deal start -> deal open -> restaurant open
                            // deal end -> deal close -> restaurant close
                            log.info("formatter :" + TIME_FORMATTER);
                            final LocalTime start = !ObjectUtils.isEmpty(deal.getStart())
                                ? LocalTime.parse(deal.getStart(), TIME_FORMATTER)
                                : !ObjectUtils.isEmpty(deal.getOpen())
                                ? LocalTime.parse(deal.getOpen(), TIME_FORMATTER)
                                : LocalTime.parse(restaurant.getOpen(), TIME_FORMATTER);

                            final LocalTime end = !ObjectUtils.isEmpty(deal.getEnd())
                                ? LocalTime.parse(deal.getEnd(), TIME_FORMATTER)
                                : !ObjectUtils.isEmpty(deal.getClose())
                                ? LocalTime.parse(deal.getClose(), TIME_FORMATTER)
                                : LocalTime.parse(restaurant.getClose(), TIME_FORMATTER);

                            return List.of(new DealStatusChangeEvent(start, true),
                                new DealStatusChangeEvent(end, false));
                        }))
                    .flatMap(List::stream)
                    .collect(Collectors.toCollection(ArrayList::new));

                final LocalTime[] firstPeakDealRange = findOverlappingDealEventAtMax(allDealStartEndTimes);
                if (firstPeakDealRange == null) {
                    return PeakDealTimeRangeResponse.builder().build();
                } else {
                    return PeakDealTimeRangeResponse.builder()
                        .peakTimeStart(firstPeakDealRange[0])
                        .peakTimeEnd(firstPeakDealRange[1])
                        .build();
                }
            });
    }

    private LocalTime[] findOverlappingDealEventAtMax(List<DealStatusChangeEvent> events) {

        // Sort the events
        Collections.sort(events);
        // hold current active deals
        int activeDeals = 0;
        // hold max active deals so far
        int maxActiveDeals = 0;
        // hold the first time range for max active deals
        LocalTime[] firstMaxRange = null;
        LocalTime currentDealStart = null;

        for (DealStatusChangeEvent event : events) {
            if (event.isDealStart) {
                // a deal has started, increment active deals
                activeDeals++;
                if (activeDeals > maxActiveDeals) {
                    // new max, update range start time and clear old range
                    maxActiveDeals = activeDeals;
                    currentDealStart = event.eventTime;
                    firstMaxRange = null; // Reset since we found a higher max
                }  else if (activeDeals == maxActiveDeals && currentDealStart == null) {
                    // Handle additional start events before end events, only valid if the previous deal start
                    // hasn't been set
                    currentDealStart = event.eventTime;
                }
            } else {
                // a deal has ended, if this is a max period, add to max ranges and decrement active deals.
                if (activeDeals == maxActiveDeals && currentDealStart != null) {
                    // Choose the first max durtion among ranges with maximum deals (this is made null when a new deal
                    // starts at activeDeals > maxActiveDeals)
                    if (firstMaxRange == null) {
                        firstMaxRange = new LocalTime[] { currentDealStart, event.eventTime };
                    }
                    currentDealStart = null;
                }
                activeDeals--;
            }
        }
        return firstMaxRange;
    }

    // specialised comparable for sorting
    public static class DealStatusChangeEvent implements Comparable<DealStatusChangeEvent> {
        LocalTime eventTime;
        boolean isDealStart;

        DealStatusChangeEvent(LocalTime eventTime, boolean isDealStart) {
            this.eventTime = eventTime;
            this.isDealStart = isDealStart;
        }

        @Override
        public int compareTo(DealStatusChangeEvent other) {
            // Sroted and handle same time even starts
            int comparison = this.eventTime.compareTo(other.eventTime);
            if (comparison != 0) {
                return comparison;
            } else {
                return Boolean.compare(!this.isDealStart, !other.isDealStart);
            }
        }
    }
}