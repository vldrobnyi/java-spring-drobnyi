package edu.ntudp.sau.spring_java.service.parser;

import edu.ntudp.sau.spring_java.model.dto.product.ProductParsingDto;
import org.openqa.selenium.JavascriptExecutor;
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
public class RozetkaParser implements Parser<ProductParsingDto> {
    private boolean isCategoryPage = false;
    private String urlTemplate = "https://rozetka.com.ua/ua/search/?text=%s";

    private final WebDriver driver;

    private static final Logger logger = LoggerFactory.getLogger(RozetkaParser.class);

    @Autowired
    public RozetkaParser(WebDriverService webDriverService) {
        this.driver = webDriverService.getDriver();
    }

    public List<ProductParsingDto> parseProducts(String search, int pageLimit) {
        try {
            search = search.trim().replaceAll(" ", "%20");

            int page = 1;

            String url = String.format(urlTemplate, search);
            driver.get(url);

            try {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                wait.until(driver -> js.executeScript("return document.readyState").equals("complete"));
            } catch (Exception e) {
                logger.error("Error during page load: {}", e.getMessage());
                return null;
            }

            makeUrlTemplate(driver.getCurrentUrl());

            logger.info("Parsing search request: {} for template: {}", search, urlTemplate);

            int maxPageNumber = Math.min(parseMaxPage(getPageUrl(search, page)), pageLimit);

            logger.info("Max page number: {}", maxPageNumber);

            List<ProductParsingDto> productParsingDtos = parseProductsPage(getPageUrl(search, page));
            logger.info("Finished page #{}", page);

            while (page < maxPageNumber) {
                makeUrlTemplate(driver.getCurrentUrl());
                page++;

                List<ProductParsingDto> parsedPageProductParsingDtos = parseProductsPage(getPageUrl(search, page));
                if (parsedPageProductParsingDtos == null || parsedPageProductParsingDtos.isEmpty()) {
                    logger.warn("No products found for page #{}", page);
                    continue;
                }

                productParsingDtos.addAll(parsedPageProductParsingDtos);
                logger.info("Finished page #{}", page);
            }

            return productParsingDtos;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private List<ProductParsingDto> parseProductsPage(String pageUrl) {
        logger.info("Parsing page url: {}", pageUrl);
        driver.get(pageUrl);

        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(driver -> js.executeScript("return document.readyState").equals("complete"));

        List<ProductParsingDto> productParsingDtos = driver.findElements(By.cssSelector("section.content_type_catalog .goods-tile")).stream().map(tile -> {
            try {
                long id = Long.parseLong(tile.findElement(By.className("g-id")).getAttribute("textContent"));
                String name = tile.findElement(By.cssSelector(".goods-tile__title")).getText();
                String link = tile.findElement(By.cssSelector(".goods-tile__content .product-link.goods-tile__heading a")).getAttribute("href");
                String priceText = tile.findElement(By.cssSelector(".goods-tile__price-value")).getText();
                double price = Double.parseDouble(priceText.replaceAll("\\s", "").replace("₴", ""));
                String stockStatus = tile.findElement(By.cssSelector(".goods-tile__availability")).getText();
                return ProductParsingDto
                        .builder()
                        .id(id).name(name)
                        .link(link)
                        .price(price)
                        .stockStatus(stockStatus)
                        .build();
            } catch (Exception e) {
                logger.error("Could not parse product: {}", e.getMessage());
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());

        return productParsingDtos;
    }

    private int parseMaxPage(String url) {
        driver.get(url);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(driver -> js.executeScript("return document.readyState").equals("complete"));

        List<WebElement> searchPaginationList = driver.findElements(By.className("pagination__item"));

        if (searchPaginationList != null && !searchPaginationList.isEmpty()) {
            return Integer.parseInt(searchPaginationList.getLast().getText());
        }

        List<WebElement> paginationList = driver.findElements(By.cssSelector("a.page"));

        if (paginationList != null && !paginationList.isEmpty()) {
            return Integer.parseInt(paginationList.getLast().getText());
        }

        return 1;
    }

    private void makeUrlTemplate(String currentUrl) {
        if (currentUrl.contains("search") && urlTemplate.contains("search") && urlTemplate.contains("page=")) {
            return;
        }

        if (currentUrl.matches(".*/c\\d+/.*")) {
            List<String> urlList = new ArrayList<>(Arrays.asList(currentUrl.split("#")));
            if (urlList.size() > 1) {
                if (currentUrl.contains("preset=")) {
                    urlList.removeLast();
                    urlTemplate = String.join("", urlList);
                    urlTemplate = urlTemplate.substring(0, urlTemplate.length() - 1) + ";page=%d";
                } else {
                    urlList.removeLast();
                    urlList.add("page=%d");
                    urlTemplate = String.join("", urlList);
                }
                isCategoryPage = true;
            } else if (currentUrl.contains("page=")) {
                urlTemplate = currentUrl.substring(0, currentUrl.length() - 2) + "%d";
                isCategoryPage = true;
            }
        }
        else if(currentUrl.contains("/producer/")) {
            List<String> urlList = new ArrayList<>(Arrays.asList(currentUrl.split("#")));

            if (urlList.size() > 1) {
                urlList.removeLast();
                urlTemplate = String.join("", urlList);
                urlTemplate +="?page=%d";
            }
            isCategoryPage = true;
        }
        else {
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
