package org.eatclub.codingchallenge.service;

import org.eatclub.codingchallenge.model.response.ActiveDealsResponse;
import reactor.core.publisher.Mono;

import java.time.LocalTime;

public interface ActiveDealsService {
  Mono<ActiveDealsResponse> getActiveDealsAt(LocalTime timeOfDay);
}
