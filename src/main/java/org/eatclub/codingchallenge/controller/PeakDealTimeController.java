package org.eatclub.codingchallenge.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eatclub.codingchallenge.model.response.PeakDealTimeRangeResponse;
import org.eatclub.codingchallenge.service.PeakDealTimeRangeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PeakDealTimeController {

    private final PeakDealTimeRangeService peakDealTimeRangeService;

    @GetMapping("/peak-time-window")
    public Mono<PeakDealTimeRangeResponse> getPeakTimeWindow() {
        return this.peakDealTimeRangeService.getPeakDealTimeRange();
    }
}
