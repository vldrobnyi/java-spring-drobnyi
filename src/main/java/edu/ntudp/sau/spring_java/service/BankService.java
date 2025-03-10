package edu.ntudp.sau.spring_java.service;

import edu.ntudp.sau.spring_java.model.dto.CurrencyDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class BankService {
    private final WebClient webClient;

    public BankService(WebClient webClient) {
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
