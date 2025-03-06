package edu.ntudp.sau.spring_java.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductAttribute {
    private Product product;
    private String name;
    private String value;

}
