package edu.ntudp.sau.spring_java.service;

import edu.ntudp.sau.spring_java.model.dto.currency.CurrencyDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class CurrencyService {
    private final WebClient webClient;

    public CurrencyService(@Qualifier("currencyWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<List<CurrencyDto>> getCurrencyRate() {
        return  this.webClient.get()
                .uri("/pubinfo?exchange&json&coursid=11")
                .retrieve()
                .bodyToFlux(CurrencyDto.class)
                .collectList();
    }
}
