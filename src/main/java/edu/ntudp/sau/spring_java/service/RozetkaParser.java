package edu.ntudp.sau.spring_java.service;

import edu.ntudp.sau.spring_java.model.dto.ProductDto;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Scope;

@Service
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RozetkaParser {
    private static final int RETRY_COUNT = 5;
    private boolean isCategoryPage = false;
    private String urlTemplate = "https://rozetka.com.ua/ua/search/?text=%s";

    private final WebDriver driver;

    private static final Logger logger = LoggerFactory.getLogger(RozetkaParser.class);

    @Autowired
    public RozetkaParser(WebDriverService webDriverService) {
        this.driver = webDriverService.getDriver();
    }

    public List<ProductDto> parseProducts(String search, int pageLimit) {
        try {
            search = search.replaceAll(" ", "%20");

            int page = 1;

            String url = String.format(urlTemplate, search);
            driver.get(url);

            try {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                wait.until(driver -> js.executeScript("return document.readyState").equals("complete"));
            } catch (Exception e) {
                return null;
            }

            makeUrlTemplate(driver.getCurrentUrl());

            logger.info("Parsing search request: {} for template: {}", search, urlTemplate);

            int maxPageNumber = Math.min(parseMaxPage(getPageUrl(search, page)), pageLimit);

            logger.info("Max page number: {}", maxPageNumber);

            List<ProductDto> productDtos = parseProductsPage(getPageUrl(search, page));
            logger.info("Finished page #{}", page);

            while (page < maxPageNumber) {
                page++;

                List<ProductDto> parsedPageProductDtos = parseProductsPage(getPageUrl(search, page));
                if (parsedPageProductDtos == null || parsedPageProductDtos.isEmpty()) {
                    logger.warn("No products found for page #{}", page);
                    continue;
                }

                productDtos.addAll(parsedPageProductDtos);
                logger.info("Finished page #{}", page);
            }

            return productDtos;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private List<ProductDto> parseProductsPage(String pageUrl) {
        driver.get(pageUrl);

        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(driver -> js.executeScript("return document.readyState").equals("complete"));

        List<ProductDto> productDtos = driver.findElements(By.cssSelector("section.content_type_catalog .goods-tile"))
                .parallelStream()
                .map(tile -> {
                    try {
                        long id = Long.parseLong(tile.findElement(By.className("g-id")).getAttribute("textContent"));
                        String name = tile.findElement(By.cssSelector(".goods-tile__title")).getText();
                        String link = tile.findElement(By.cssSelector(".goods-tile__content .product-link.goods-tile__heading a")).getAttribute("href");
                        String priceText = tile.findElement(By.cssSelector(".goods-tile__price-value")).getText();
                        double price = Double.parseDouble(priceText.replaceAll("\\s", "").replace("₴", ""));

                        String stockStatus = tile.findElements(By.cssSelector(".goods-tile__availability"))
                                .stream()
                                .findFirst()
                                .map(WebElement::getText)
                                .orElse("Unknown");

                        return ProductDto
                                .builder()
                                .id(id)
                                .name(name)
                                .link(link)
                                .price(price)
                                .stockStatus(stockStatus)
                                .build();
                    } catch (Exception e) {
                        logger.error("Could not parse product: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return productDtos;
    }

    private ProductDto parseSingleProduct(String productUrl) {
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


                ProductDto productDto = ProductDto
                        .builder()
                        .id(id)
                        .name(productName)
                        .stockStatus(stockStatus)
                        .price(price)
                        .link(productUrl)
                        .build();


                System.out.println(productDto.toString());
                return productDto;
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
                JavascriptExecutor js = (JavascriptExecutor) driver;
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                wait.until(driver -> js.executeScript("return document.readyState").equals("complete"));

                List<WebElement> paginationList = driver.findElements(By.cssSelector("a.page"));
                return Integer.parseInt(paginationList.getLast().getText());
            } catch (Exception e) {
                return 1;
            }
        } else {
            try {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                wait.until(driver -> js.executeScript("return document.readyState").equals("complete"));

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
