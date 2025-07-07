package org.eatclub.codingchallenge.service;

import org.eatclub.codingchallenge.model.response.PeakDealTimeRangeResponse;
import reactor.core.publisher.Mono;

public interface PeakDealTimeRangeService {

    Mono<PeakDealTimeRangeResponse> getPeakDealTimeRange();
}
