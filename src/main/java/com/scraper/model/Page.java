package com.scraper.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Page{
  private List<Restaurant> restaurants;
}
