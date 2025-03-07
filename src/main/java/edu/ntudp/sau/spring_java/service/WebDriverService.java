package edu.ntudp.sau.spring_java.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Getter
public class WebDriverService {
    private final WebDriver driver;

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
            driver.quit();
        }
    }
}
