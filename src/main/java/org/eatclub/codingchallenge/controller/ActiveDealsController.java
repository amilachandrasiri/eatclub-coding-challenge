package org.eatclub.codingchallenge.controller;

import lombok.RequiredArgsConstructor;
import org.eatclub.codingchallenge.model.response.ActiveDealsResponse;
import org.eatclub.codingchallenge.service.ActiveDealsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class ActiveDealsController {

  private final ActiveDealsService activeDealsService;

  @GetMapping("/active-deals")
  public Mono<ActiveDealsResponse> getActiveDeals(@RequestParam("timeOfDay") String timeOfDay) {
    return this.activeDealsService.getActiveDealsAt(timeOfDay);
  }

  @GetMapping("/peak-time-window")
  public Mono<String> getPeakTimeWindow() {
    return Mono.just("Peak time window data");
  }
}
