package com.scraper.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Address {
  private String address;
  private String zipCode;
}
