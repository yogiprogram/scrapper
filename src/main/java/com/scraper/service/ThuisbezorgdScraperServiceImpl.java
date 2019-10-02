package com.scraper.service;

import com.scraper.model.Address;
import com.scraper.model.Delivery;
import com.scraper.model.Page;
import com.scraper.model.Restaurant;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThuisbezorgdScraperServiceImpl implements ScraperService {
  String RESTAURANT_CARD_CSS_QUERY = ".grid-container [id=irestaurantlist] .restaurant";
  String RESTAURANT_CARD_DETAILS_CSS_QUERY = ".detailswrapper h2 > a";
  String RESTAURANT_CUISINE_DETAILS_CSS_QUERY = ".detailswrapper .kitchens";
  String RESTAURANT_CARD_RATING_CSS_QUERY = ".review-rating .rating-total";
  String INFO_ADDRESS_CSS_QUERY = ".infoCardList .restaurant-info__imprint .infoTabSection";
  String INFO_RESTAURANT_URL_CSS_QUERY =
      ".infoCardList .restaurant-info__restaurant-link .infoTabSection a";
  String AVERAGE_TIME_CSS_QUERY = ".detailswrapper .avgdeliverytime";
  String MIN_ORDER_FEE_CSS_QUERY = ".detailswrapper .min-order";
  String DELIVERY_COST_CSS_QUERY = ".detailswrapper .delivery-cost";
  Pattern ZIP_CODE_PATTERN = Pattern.compile("\\d{4}\\w{2}");
  String DELIVERY_TIME_ONLY_DIGIT_PATTERN = "\\D";
  String DELIVERY_FEE_PATTERN = "[^\\d\\, ]| \\,|\\,$";

  public Page start(String urlToCrawl) throws IOException {
    Document htmlPage = Jsoup.connect(urlToCrawl).get();
    return Page.builder().restaurants(getRestaurants(htmlPage)).build();
  }

  private List<Restaurant> getRestaurants(Document htmlPage) {
    List<Restaurant> restaurants = new ArrayList<>();
    Elements restaurantCards = htmlPage.select(RESTAURANT_CARD_CSS_QUERY);
    restaurantCards
        .parallelStream()
        .forEach(
            restaurantCard -> {
              Elements urlAndName = restaurantCard.select(RESTAURANT_CARD_DETAILS_CSS_QUERY);
              Elements cuisineList = restaurantCard.select(RESTAURANT_CUISINE_DETAILS_CSS_QUERY);
              String restaurantPortalUrl = getRestaurantPortalUrl(restaurantCard, urlAndName);
              String restaurantPortalInfoUrl = restaurantPortalUrl.concat("#info");
              restaurants.add(
                  Restaurant.builder()
                      .currency("€")
                      .name(urlAndName.text())
                      .cuisineDetails(cuisineList.text())
                      .totalRating(getTotalRatingTotal(restaurantCard))
                      .portalUrl(restaurantPortalUrl)
                      .minimumOrderAmount(getMinimumOrderAmount(restaurantCard))
                      .deliveryFees(getDeliveryFees(restaurantCard, DELIVERY_FEE_PATTERN))
                      .avgDeliveryTime(
                          getAverageDeliveryTime(restaurantCard, DELIVERY_TIME_ONLY_DIGIT_PATTERN))
                      .portalInfoUrl(restaurantPortalInfoUrl)
                      .delivery(getDelivery(restaurantPortalInfoUrl))
                      .build());
            });
    return restaurants;
  }

  private int getTotalRatingTotal(Element restaurantCard) {
    int ratingTotal = 0;
    try {
      String ratingText = restaurantCard.select(RESTAURANT_CARD_RATING_CSS_QUERY).text();
      if (ratingText != null && ratingText.length() > 0) {
        ratingTotal = Integer.parseInt(ratingText.replaceAll("[^\\d.]", ""));
      }
    } catch (Exception e) {
        ratingTotal = 0;
    }
    return ratingTotal;
  }

  private double getMinimumOrderAmount(Element restaurantCard) {
    String minOrder = restaurantCard.select(MIN_ORDER_FEE_CSS_QUERY).text(); // Min. € 10,00
    if (minOrder != null && minOrder.isEmpty()) {
      minOrder = "0";
    }
    return new Double(minOrder.replaceAll(DELIVERY_FEE_PATTERN, "").replaceAll(",", "."));
  }

  private Double getDeliveryFees(Element restaurantCard, String costPattern) {
    String deliveryFees = restaurantCard.select(DELIVERY_COST_CSS_QUERY).text(); // € 10,00
    deliveryFees = deliveryFees.replaceAll(costPattern, "");
    if (deliveryFees.length() == 0) {
      deliveryFees = "0";
    } else {
      deliveryFees = deliveryFees.replaceAll(",", ".");
      if (deliveryFees.length() == 0) {
        deliveryFees = "0";
      }
    }
    return new Double(deliveryFees);
  }

  private String getAverageDeliveryTime(Element restaurantCard, String timePattern) {
    String avgDeliveryTime = restaurantCard.select(AVERAGE_TIME_CSS_QUERY).text(); // est.40min
    if (avgDeliveryTime.contains("est.")) {
      avgDeliveryTime = avgDeliveryTime.replaceAll(timePattern, "");
    } else {
      avgDeliveryTime = "0";
    }
    return avgDeliveryTime;
  }

  private Delivery getDelivery(String restaurantInfoUrl) {
    try {
      Document infoHtmlPage =
          Jsoup.connect(restaurantInfoUrl)
              .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
              .maxBodySize(0)
              .timeout(600000)
              .get();
      return Delivery.builder()
          .address(getAddress(infoHtmlPage))
          .restaurantUrl(getRestaurantUrl(infoHtmlPage))
          .build();
    } catch (Exception e) {
      return Delivery.builder().build();
    }
  }

  private String getRestaurantUrl(Document infoHtmlPage) {
    return infoHtmlPage.select(INFO_RESTAURANT_URL_CSS_QUERY).attr("href");
  }

  private Address getAddress(Document htmlPage) {
    Elements elements = htmlPage.select(INFO_ADDRESS_CSS_QUERY);
    String addressText = elements.text();
    Matcher matcher = ZIP_CODE_PATTERN.matcher(addressText);
    String zipCode = null;
    if (matcher.find()) {
      zipCode = matcher.group();
    }
    return Address.builder().address(addressText).zipCode(zipCode).build();
  }

  private String getRestaurantPortalUrl(Element restaurantCard, Elements urlAndName) {
    String documentLocation = urlAndName.attr("href");
    String baseUrl = restaurantCard.baseUri();
    return baseUrl.substring(0, baseUrl.indexOf("/en")).concat(documentLocation);
  }
}
