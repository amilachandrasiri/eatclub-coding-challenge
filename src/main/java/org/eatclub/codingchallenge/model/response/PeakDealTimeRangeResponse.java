package org.eatclub.codingchallenge.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class PeakDealTimeRangeResponse {
    @JsonProperty("peakTimeStart")
    private LocalTime peakTimeStart;

    @JsonProperty("peakTimeEnd")
    private LocalTime peakTimeEnd;
}
