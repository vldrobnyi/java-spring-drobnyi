package edu.ntudp.sau.spring_java.service;

import edu.ntudp.sau.spring_java.model.Product;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductParser {
    //private static final String ROZETKA_URL = "https://rozetka.com.ua/ua/search/?text=%s";
    private static final int RETRY_COUNT = 5;
    private boolean isCategoryPage = false;
    private String urlTemplate = "https://rozetka.com.ua/ua/search/?text=%s";

    private final WebDriver driver;

    @Autowired
    public ProductParser(WebDriverService webDriverService) {
        this.driver = webDriverService.getDriver();
    }

    public List<Product> parseProducts(String search, int pageLimit) {
        try {
            search = search.replaceAll(" ", "%20");

            int page = 1;
            String url = String.format(urlTemplate, search);

            driver.get(url);

            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("section.content_type_catalog")));
            } catch (Exception e) {
                return null;
            }

            makeUrlTemplate(driver.getCurrentUrl());

            System.out.println(getPageUrl(search, page));
            int maxPageNumber = Math.min(parseMaxPage(getPageUrl(search, page)), pageLimit);

            System.out.println(maxPageNumber);

            List<Product> products = parseProductsPage(getPageUrl(search, page));


            while (page < maxPageNumber) {
                page++;
                long startTime = System.currentTimeMillis();
                List<Product> parsedPageProducts = parseProductsPage(getPageUrl(search, page));
                long endTime = System.currentTimeMillis();

                System.out.println("Time taken: " + (endTime - startTime));

                if (parsedPageProducts == null || parsedPageProducts.isEmpty()) {
                    break;
                }

                products.addAll(parsedPageProducts);
                System.out.println("Finished page #" + page);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private List<Product> parseProductsPage(String pageUrl) {
        driver.get(pageUrl);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("section.content_type_catalog .goods-tile .goods-tile__content .product-link.goods-tile__heading a")));

        List<WebElement> productLinks = driver.findElements(By.cssSelector("section.content_type_catalog .goods-tile .goods-tile__content .product-link.goods-tile__heading a"));
        List<String> links = driver.findElements(By.cssSelector("section.content_type_catalog .goods-tile .goods-tile__content .product-link.goods-tile__heading a"))
                .stream()
                .map(e -> e.getAttribute("href"))
                .collect(Collectors.toList());

        List<Product> products = new ArrayList<>();

        for (String link : links) {
            Product product = parseSingleProduct(link);
            if (product != null) {
                products.add(product);
            }
        }
        return products;
    }

    private Product parseSingleProduct(String productUrl) {
        System.out.println("Now parsing: " + productUrl);

        driver.get(productUrl);

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-about__right")));
        } catch (Exception e) {
            System.out.println("Invalid product page");
            return null;
        }

        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                String ratingText = driver.findElement(By.cssSelector(".product-about__right div.rating.text-base span")).getText();
                var idArray = ratingText.split("\\s");
                long id = Long.parseLong(idArray[idArray.length - 1]);
                String productName = driver.findElement(By.cssSelector(".product-about__right h1.title__font")).getText();
                WebElement stockStatusElement = driver.findElements(By.cssSelector(".product-about__right p.status-label"))
                        .stream()
                        .findFirst()
                        .orElse(null);
                String stockStatus = (stockStatusElement != null) ? stockStatusElement.getText() : "Not Available";
                double price = Double.parseDouble(driver.findElement(By.cssSelector(".product-about__right p.product-price__big")).getText().replaceAll("\\s", "").replace("₴", ""));


                Product product = Product
                        .builder()
                        .id(id)
                        .name(productName)
                        .stockStatus(stockStatus)
                        .price(price)
                        .link(productUrl)
                        .build();


                System.out.println(product.toString());
                return product;
            } catch (Exception e) {
                System.out.println("Attempt " + i + " failed");
            }
        }
        System.out.println("Parsing failed");
        return null;
    }

    private int parseMaxPage(String url) {
        driver.get(url);
        if (isCategoryPage) {
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
                wait.until(ExpectedConditions.presenceOfElementLocated(By.className("paginator")));

                List<WebElement> paginationList = driver.findElements(By.cssSelector("a.page"));
                return Integer.parseInt(paginationList.getLast().getText());
            } catch (Exception e) {
                return 1;
            }
        } else {
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
                wait.until(ExpectedConditions.presenceOfElementLocated(By.className("pagination__item")));

                List<WebElement> paginationList = driver.findElements(By.className("pagination__item"));
                return Integer.parseInt(paginationList.getLast().getText());
            } catch (Exception e) {
                return 1;
            }
        }
    }

    private void makeUrlTemplate(String currentUrl) {
        if (currentUrl.matches(".*/c\\d+/.*")) {
            List<String> urlList = new ArrayList<>(Arrays.asList(currentUrl.split("#")));
            urlList.removeLast();
            urlList.add("page=%d");
            urlTemplate = String.join("", urlList);
            isCategoryPage = true;
        } else {
            urlTemplate += "&page=%d";
        }
    }

    private String getPageUrl(String search, int page) {
        if (isCategoryPage) {
            return String.format(urlTemplate, page);
        }

        return String.format(urlTemplate, search, page);
    }
}
