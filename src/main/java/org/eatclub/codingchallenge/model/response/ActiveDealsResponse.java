package org.eatclub.codingchallenge.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActiveDealsResponse {

  @JsonProperty("deals")
  private List<DealsItem> deals;
}
