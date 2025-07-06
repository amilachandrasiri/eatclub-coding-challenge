package org.eatclub.codingchallenge.controller;

import lombok.RequiredArgsConstructor;
import org.eatclub.codingchallenge.service.ActiveDealsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class ActiveDealsController {

    private final ActiveDealsService activeDealsService;

    @GetMapping("/active-deals")
    public Mono<String> getActiveDeals(String timeOfDay) {
        return this.activeDealsService.getActiveDeals(timeOfDay);
    }

    @GetMapping("/peak-time-window")
    public Mono<String> getPeakTimeWindow() {
        return Mono.just("Peak time window data");
    }

}
