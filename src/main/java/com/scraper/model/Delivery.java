package com.scraper.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Delivery {
  private Address address;
  private String restaurantUrl;
}
