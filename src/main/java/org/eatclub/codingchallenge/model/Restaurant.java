package org.eatclub.codingchallenge.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Restaurant {

  @JsonProperty("imageLink")
  private String imageLink;

  @JsonProperty("address1")
  private String address1;

  @JsonProperty("deals")
  private List<Deals> deals;

  @JsonProperty("name")
  private String name;

  @JsonProperty("suburb")
  private String suburb;

  @JsonProperty("close")
  private String close;

  @JsonProperty("objectId")
  private String objectId;

  @JsonProperty("open")
  private String open;

  @JsonProperty("cuisines")
  private List<String> cuisines;
}
