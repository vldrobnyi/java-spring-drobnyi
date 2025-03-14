package edu.ntudp.sau.spring_java.model.dto.currency;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CurrencyDto {
    @JsonProperty("ccy")
    private String currencyCode;

    @JsonProperty("base_ccy")
    private String baseCurrencyCode;

    @JsonProperty("buy")
    private double buyRate;

    @JsonProperty("sale")
    private double sellRate;
}
