package edu.ntudp.sau.spring_java.model.dto.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponseDto {
    private Long id;
    private String name;
    private double priceUah;
    private double priceUsd;
    private double priceEur;
    private String stockStatus;
    private String link;
}
