package edu.ntudp.sau.spring_java.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Product {
    @Id
    private Long id;
    private String name;
    private double priceUah;
    private double priceUsd;
    private double priceEur;
    private String stockStatus;
    private String link;
    private LocalDateTime additionDateTime;
    private LocalDateTime lastUpdateDateTime;
}
