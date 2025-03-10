package edu.ntudp.sau.spring_java.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class BankWebClientConfig {

    @Bean
    public WebClient bankWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://api.privatbank.ua/p24api")
                .build();
    }
}
