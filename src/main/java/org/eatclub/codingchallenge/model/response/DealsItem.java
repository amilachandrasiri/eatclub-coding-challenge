package org.eatclub.codingchallenge.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DealsItem {

  @JsonProperty("restaurantObjectId")
  private String restaurantObjectId;

  @JsonProperty("restaurantName")
  private String restaurantName;

  @JsonProperty("restaurantAddress1")
  private String restaurantAddress1;

  @JsonProperty("restarantSuburb")
  private String restarantSuburb;

  @JsonProperty("restaurantOpen")
  private String restaurantOpen;

  @JsonProperty("restaurantClose")
  private String restaurantClose;

  @JsonProperty("dealObjectId")
  private String dealObjectId;

  @JsonProperty("discount")
  private String discount;

  @JsonProperty("dineIn")
  private boolean dineIn;

  @JsonProperty("lightning")
  private boolean lightning;

  @JsonProperty("qtyLeft")
  private long qtyLeft;
}
