package org.eatclub.codingchallenge.util;

import org.eatclub.codingchallenge.model.Deals;
import org.eatclub.codingchallenge.model.Restaurant;
import org.eatclub.codingchallenge.model.response.DealsItem;
import org.springframework.stereotype.Component;

@Component
public class DefaultActiveDealsResponseMapper implements ActiveDealsResponseMapper {

    public DealsItem mapToDealsItem(final Restaurant restaurant, final Deals deal) {
        return DealsItem.builder()
            .restaurantObjectId(restaurant.getObjectId())
            .restaurantName(restaurant.getName())
            .restaurantAddress1(restaurant.getAddress1())
            .restarantSuburb(restaurant.getSuburb())
            .restaurantOpen(restaurant.getOpen())
            .restaurantClose(restaurant.getClose())
            .dealObjectId(deal.getObjectId())
            .discount(deal.getDiscount())
            .dineIn(deal.isDineIn())
            .lightning(deal.isLightning())
            .qtyLeft(deal.getQtyLeft())
            .build();
    }
}
