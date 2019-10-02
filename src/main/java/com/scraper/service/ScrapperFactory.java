package com.scraper.service;

import com.scraper.model.Page;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ScrapperFactory {
  private static Map<String, ScraperService> regions = new HashMap<>();

  static {
    // "https://www.thuisbezorgd.nl/en/order-takeaway-amsterdam-stadsdeel-binnenstad-1011"
    regions.put("thuisbezorgd", new ThuisbezorgdScraperServiceImpl());
  }

  public static Page start(String domainName, String urlToCrawl) throws IOException {
    Optional<ScraperService> scraperService =
        Optional.ofNullable(regions.get(domainName.toLowerCase()));
    scraperService.orElseThrow(() -> new IllegalArgumentException("Invalid regions name"));
    return scraperService.get().start(urlToCrawl);
  }
}
