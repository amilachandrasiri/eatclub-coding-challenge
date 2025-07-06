package org.eatclub.codingchallenge.service;

import org.eatclub.codingchallenge.model.response.ActiveDealsResponse;
import reactor.core.publisher.Mono;

public interface ActiveDealsService {
  Mono<ActiveDealsResponse> getActiveDealsAt(String timeOfDay);
}
