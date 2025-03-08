package edu.ntudp.sau.spring_java.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private long id;
    private String name;
    private double price;
    private String stockStatus;
    private String link;
}
