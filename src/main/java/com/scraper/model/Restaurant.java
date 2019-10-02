package com.scraper.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Restaurant {
  private String name;
  private String portalUrl;
  private String portalInfoUrl;
  private String avgDeliveryTime;
  private String currency;
  private String cuisineDetails;
  private int totalRating;
  private double deliveryFees;
  private double minimumOrderAmount;
  private Delivery delivery;
}
