package edu.ntudp.sau.spring_java.service;

import edu.ntudp.sau.spring_java.model.dto.currency.CurrencyDto;
import edu.ntudp.sau.spring_java.model.dto.product.ProductParsingDto;
import edu.ntudp.sau.spring_java.model.dto.product.ProductResponseDto;
import edu.ntudp.sau.spring_java.model.entity.Product;
import edu.ntudp.sau.spring_java.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CurrencyService currencyService;

    @Autowired
    public ProductService(ProductRepository productRepository, CurrencyService currencyService) {
        this.productRepository = productRepository;
        this.currencyService = currencyService;
    }

    public List<ProductResponseDto> saveProducts(List<ProductParsingDto> productParsingDtos) {
        List<CurrencyDto> currencies = currencyService.getCurrencyRate()
                .block();

        if (currencies == null || currencies.isEmpty()) {
            throw new RuntimeException("Currency data is unavailable.");
        }

        List<ProductResponseDto> savedProducts = new ArrayList<>();

        for (ProductParsingDto productParsingDto : productParsingDtos) {
            Optional<Product> existingProductOpt = productRepository.findById(productParsingDto.getId());
            Product product;

            if (existingProductOpt.isPresent()) {
                product = existingProductOpt.get();
                product.setName(productParsingDto.getName());
                product.setPriceUah(productParsingDto.getPrice());
                product.setStockStatus(productParsingDto.getStockStatus());
                product.setLink(productParsingDto.getLink());
                product.setLastUpdateDate(new Date());
            } else {
                product = convertToEntity(productParsingDto);
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

            savedProducts.add(convertToResponseDto(productRepository.save(product)));
        }

        return savedProducts;
    }

    public List<ProductResponseDto> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    private Product convertToEntity(ProductParsingDto productParsingDto) {
        return Product
                .builder()
                .id(productParsingDto.getId())
                .priceUah(productParsingDto.getPrice())
                .name(productParsingDto.getName())
                .link(productParsingDto.getLink())
                .stockStatus(productParsingDto.getStockStatus())
                .creationDate(new Date())
                .lastUpdateDate(new Date())
                .build();
    }

    private ProductResponseDto convertToResponseDto(Product product) {
        return ProductResponseDto
                .builder()
                .id(product.getId())
                .name(product.getName())
                .priceUah(product.getPriceUah())
                .priceUsd(product.getPriceUsd())
                .priceEur(product.getPriceEur())
                .stockStatus(product.getStockStatus())
                .link(product.getLink())
                .build();
    }
}

