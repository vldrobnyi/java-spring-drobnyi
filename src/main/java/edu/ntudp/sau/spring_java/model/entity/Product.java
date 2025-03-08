package edu.ntudp.sau.spring_java.model.entity;

import java.time.LocalDateTime;

public class Product {
    private long id;
    private String name;
    private double priceUah;
    private double priceUsd;
    private double priceEur;
    private String stockStatus;
    private String link;
    private LocalDateTime additionDateTime;
    private LocalDateTime lastUpdateDateTime;
}
