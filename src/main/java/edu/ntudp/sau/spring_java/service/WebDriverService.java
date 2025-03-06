package edu.ntudp.sau.spring_java.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

@Component
@Getter
public class WebDriverService {
    private final WebDriver driver;

    public WebDriverService() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Run in the background
        options.addArguments("--disable-gpu"); // Improve performance
        options.addArguments("--disable-images"); // Improve performance
        options.addArguments("--no-sandbox"); // Improve performance
        options.addArguments("--window-size=1920,1080");

        this.driver = new ChromeDriver(options);
    }

    @PreDestroy
    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }
}
