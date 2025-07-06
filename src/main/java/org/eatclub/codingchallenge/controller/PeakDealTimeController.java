package org.eatclub.codingchallenge.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Controller
public class PeakDealTimeController {

    @GetMapping("/peak-time-window")
    public Mono<String> getPeakTimeWindow() {
        return Mono.just("Peak time window data");
    }
}
