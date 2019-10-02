package com.scraper.service;

import com.scraper.model.Page;

import java.io.IOException;

public interface ScraperService {
  Page start(String urlToCrawl) throws IOException;
}
