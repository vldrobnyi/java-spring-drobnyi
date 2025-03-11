package edu.ntudp.sau.spring_java.service.parser;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Getter
@Scope(value = "request")
public class WebDriverService {
    private final WebDriver driver;
    private static final Logger logger = LoggerFactory.getLogger(WebDriverService.class);

    public WebDriverService() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.NONE);
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-images");
        options.addArguments("--no-sandbox");
        options.addArguments("--window-size=1280,720");

        this.driver = new ChromeDriver(options);
    }

    @PreDestroy
    public void close() {
        if (driver != null) {
            logger.info("Closing driver");
            driver.quit();
        }
    }
}
