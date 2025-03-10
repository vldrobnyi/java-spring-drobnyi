package edu.ntudp.sau.spring_java.service;

import edu.ntudp.sau.spring_java.model.dto.CurrencyDto;
import edu.ntudp.sau.spring_java.model.dto.ProductDto;
import edu.ntudp.sau.spring_java.model.entity.Product;
import edu.ntudp.sau.spring_java.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final BankService bankService;

    @Autowired
    public ProductService(ProductRepository productRepository, BankService bankService) {
        this.productRepository = productRepository;
        this.bankService = bankService;
    }

    public List<Product> saveOrUpdateProducts(List<ProductDto> productDtos) {
        List<CurrencyDto> currencies = bankService.getCurrencyRate()
                .block();

        if (currencies == null || currencies.isEmpty()) {
            throw new RuntimeException("Currency data is unavailable.");
        }

        List<Product> savedProducts = new ArrayList<>();

        for (ProductDto productDto : productDtos) {
            Optional<Product> existingProductOpt = productRepository.findById(productDto.getId());
            Product product;

            if (existingProductOpt.isPresent()) {
                product = existingProductOpt.get();
                product.setName(productDto.getName());
                product.setPriceUah(productDto.getPrice());
                product.setStockStatus(productDto.getStockStatus());
                product.setLink(productDto.getLink());
                product.setLastUpdateDateTime(LocalDateTime.now());
            } else {
                product = Product
                        .builder()
                        .id(productDto.getId())
                        .priceUah(productDto.getPrice())
                        .name(productDto.getName())
                        .link(productDto.getLink())
                        .stockStatus(productDto.getStockStatus())
                        .additionDateTime(LocalDateTime.now())
                        .lastUpdateDateTime(LocalDateTime.now())
                        .build();
            }

            CurrencyDto usdCurrency = currencies.stream()
                    .filter(currency -> "USD".equals(currency.getCurrencyCode()))
                    .findFirst()
                    .orElse(null);

            CurrencyDto eurCurrency = currencies.stream()
                    .filter(currency -> "EUR".equals(currency.getCurrencyCode()))
                    .findFirst()
                    .orElse(null);

            if (usdCurrency != null) {
                BigDecimal usdPrice = new BigDecimal(product.getPriceUah() / usdCurrency.getBuyRate())
                        .setScale(2, RoundingMode.HALF_UP);
                product.setPriceUsd(usdPrice.doubleValue());
            }

            if (eurCurrency != null) {
                BigDecimal eurPrice = new BigDecimal(product.getPriceUah() / eurCurrency.getBuyRate())
                        .setScale(2, RoundingMode.HALF_UP);
                product.setPriceEur(eurPrice.doubleValue());
            }

            savedProducts.add(productRepository.save(product));
        }

        return savedProducts;
    }
}

