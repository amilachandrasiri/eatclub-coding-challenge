package org.eatclub.codingchallenge.util;

import org.eatclub.codingchallenge.model.Deals;
import org.eatclub.codingchallenge.model.Restaurant;
import org.eatclub.codingchallenge.model.response.DealsItem;

public interface ActiveDealsResponseMapper {
    DealsItem mapToDealsItem(final Restaurant restaurant, final Deals deal);
}
