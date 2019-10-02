package com.scraper.service;

import com.scraper.model.Page;
import com.scraper.model.Restaurant;

import java.util.*;
import java.util.function.Predicate;

public class BotService {
  public void showDeliveryFeesForArea(Page page) {
    String formatHeader = "%s \t\t %s";
    String format = "%s %s \t\t %s";
    System.out.println(String.format(formatHeader, "Delivery Fees", "Restaurant Name"));
    page.getRestaurants().stream()
        .forEach(
            restaurant -> {
              System.out.println(
                  String.format(
                      format,
                      restaurant.getCurrency(),
                      restaurant.getDeliveryFees(),
                      restaurant.getName()));
            });
  }

  public void deliveryTimes(Page page) {
    IntSummaryStatistics times =
        page.getRestaurants().stream()
            .filter(restaurant -> !"0".equalsIgnoreCase(restaurant.getAvgDeliveryTime()))
            .mapToInt(restaurant -> Integer.parseInt(restaurant.getAvgDeliveryTime()))
            .summaryStatistics();
    if (times != null
        && times.getMax() == Integer.MAX_VALUE
        && times.getMin() == Integer.MIN_VALUE) {
      System.out.println("No restaurant is open now");
    } else {
      System.out.println("Minimum Delivery Times : " + times.getMin() + " Min");
      System.out.println("Average Delivery Times : " + (int) times.getAverage() + " Min");
      System.out.println("Maximum Delivery Times : " + times.getMax() + " Min");
    }
  }

  public void listOfZipCodes(Page page) {
    System.out.println(" List of zip codes (restaurant location) \n ");
    page.getRestaurants().stream()
        .filter(
            restaurant ->
                restaurant != null
                    && restaurant.getDelivery() != null
                    && restaurant.getDelivery().getAddress() != null
                    && restaurant.getDelivery().getAddress().getZipCode() != null)
        .map(restaurant -> restaurant.getDelivery().getAddress().getZipCode())
        .distinct()
        .forEach(System.out::println);
  }

  public void findMaxRatingCuisine(Page page) {
    Map<String, Long> sumCuisineRating = new HashMap<>();
    Predicate<Restaurant> restaurantCuisineDetailsPredicate =
        restaurant ->
            restaurant != null
                && restaurant.getCuisineDetails() != null
                && !restaurant.getCuisineDetails().isEmpty();
    page.getRestaurants().stream()
        .filter(restaurantCuisineDetailsPredicate)
        .forEach(
            restaurant -> {
              List<String> list = Arrays.asList(restaurant.getCuisineDetails().split(","));
              for (String cuisine : list) {
                if (Objects.nonNull(cuisine)) {
                  cuisine = cuisine.trim().toLowerCase();
                  Long cuisineRating = sumCuisineRating.get(cuisine);
                  if (cuisineRating == null) {
                    sumCuisineRating.put(cuisine, (long) restaurant.getTotalRating());
                  } else {
                    sumCuisineRating.put(cuisine, cuisineRating + restaurant.getTotalRating());
                  }
                }
              }
            });
    Optional<Map.Entry<String, Long>> maxEntry =
        sumCuisineRating.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue));
    Map.Entry<String, Long> maxCuisine = maxEntry.get();
    System.out.println(
        String.format(
            " '%s' get Maximum number of reviews [%s] overall in region.",
            maxCuisine.getKey(), maxCuisine.getValue()));
  }

  public void countRestaurantUrl(Page page) {
    long totalRestaurant = page.getRestaurants().size();
    long totalRestaurantWithoutUrl =
        page.getRestaurants().stream()
            .filter(
                restaurant ->
                    Objects.nonNull(restaurant)
                        && Objects.nonNull(restaurant.getDelivery())
                        && isNull(restaurant.getDelivery().getRestaurantUrl()))
            .count();
    System.out.println("Total Number of Restaurant : " + totalRestaurant);
    System.out.println("Total Number of Restaurant without url: " + totalRestaurantWithoutUrl);
  }

  private boolean isNull(String restaurantUrl) {
    return Objects.nonNull(restaurantUrl) && restaurantUrl.isEmpty();
  }
}
