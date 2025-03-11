package edu.ntudp.sau.spring_java.model.dto.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductParsingDto {
    private long id;
    private String name;
    private double price;
    private String stockStatus;
    private String link;
}
