package com.scraper;

import com.scraper.model.Page;
import com.scraper.service.BotService;
import com.scraper.service.ScrapperFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

public class Application {
  public static void main(String[] args) throws IOException {
    String urlToCrawl =
        "https://www.thuisbezorgd.nl/en/order-takeaway-amsterdam-stadsdeel-binnenstad-1011";
    String domainName = "thuisbezorgd";
    if (Objects.nonNull(args) && args.length >= 1) {
      domainName = args[0];
      urlToCrawl = args[1];
    }

    System.out.println("Please wait for sometime while Scrapping data from " + urlToCrawl);
    System.out.println("Data loading ... ");
    Page page = ScrapperFactory.start(domainName, urlToCrawl);
    selectBotOptions(page);
  }

  private static void selectBotOptions(Page page) {
    BotService botService = new BotService();
    while (true) {
      System.out.println(
          "\n--------------------------------- Select One of Below Option ---------------------------------\n");
      System.out.println(
          "1. The distribution of delivery fees being charged by different restaurants in that area.");
      System.out.println("2. Minimum, Maximum and Average delivery times for the region.");
      System.out.println(
          "3. Determining the number of zip codes (restaurant location) delivering to that delivery area.");
      System.out.println(
          "4. Determining which food cuisine gets the maximum number of reviews overall in this region.");
      System.out.println(
          "5. Bonus A: How many of the restaurants have a website being listed to the customer");
      System.out.println("Press option as per above. [ press N for exit.]");
      Scanner ans = new Scanner(System.in);
      String answer = ans.nextLine();
      System.out.println(
          "\n--------------------------------- Result --------------------------------- \n");
      if (!answer.equalsIgnoreCase("N")) {
        switch (answer.trim()) {
          case "1":
            botService.showDeliveryFeesForArea(page);
            break;
          case "2":
            botService.deliveryTimes(page);
            break;
          case "3":
            botService.listOfZipCodes(page);
            break;
          case "4":
            botService.findMaxRatingCuisine(page);
            break;
          case "5":
            botService.countRestaurantUrl(page);
            break;
        }
      } else if (answer.equalsIgnoreCase("N")) {
        System.out.print("Thank you !!");
        break;
      }
    }
  }
}
