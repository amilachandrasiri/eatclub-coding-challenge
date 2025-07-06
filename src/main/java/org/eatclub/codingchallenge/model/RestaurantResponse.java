package org.eatclub.codingchallenge.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestaurantResponse {

  @JsonProperty("restaurants")
  private List<Restaurant> restaurants;
}
