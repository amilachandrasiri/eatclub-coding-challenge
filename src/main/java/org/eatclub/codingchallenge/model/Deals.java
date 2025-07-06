package org.eatclub.codingchallenge.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Deals {

  @JsonProperty("lightning")
  private boolean lightning;

  @JsonProperty("qtyLeft")
  private long qtyLeft;

  @JsonProperty("discount")
  private String discount;

  @JsonProperty("objectId")
  private String objectId;

  @JsonProperty("dineIn")
  private boolean dineIn;

  @JsonProperty("close")
  private String close;

  @JsonProperty("open")
  private String open;

  @JsonProperty("start")
  private String start;

  @JsonProperty("end")
  private String end;
}
