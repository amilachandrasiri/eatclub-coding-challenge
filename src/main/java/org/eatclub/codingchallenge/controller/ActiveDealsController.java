package org.eatclub.codingchallenge.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.eatclub.codingchallenge.model.response.ActiveDealsResponse;
import org.eatclub.codingchallenge.service.ActiveDealsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalTime;

@RestController
@RequiredArgsConstructor
public class ActiveDealsController {

    private final ActiveDealsService activeDealsService;

    @GetMapping("/active-deals")
    public Mono<ActiveDealsResponse> getActiveDeals(@RequestParam("timeOfDay")
                                                        @Valid
                                                        @DateTimeFormat(pattern = "h:mm[a]") LocalTime timeOfDay) {
        return this.activeDealsService.getActiveDealsAt(timeOfDay);
    }
}
