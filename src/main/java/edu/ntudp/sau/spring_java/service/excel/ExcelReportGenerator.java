package edu.ntudp.sau.spring_java.service.excel;

import edu.ntudp.sau.spring_java.model.dto.product.ProductParsingDto;
import edu.ntudp.sau.spring_java.model.dto.product.ProductResponseDto;
import edu.ntudp.sau.spring_java.model.entity.Product;

import java.util.List;

public interface ExcelReportGenerator {
    byte[] generateSearchReport(String search, List<ProductParsingDto> productParsingDtos);
    byte[] generateDatabaseReport(List<ProductResponseDto> productReposnseDtos);
}