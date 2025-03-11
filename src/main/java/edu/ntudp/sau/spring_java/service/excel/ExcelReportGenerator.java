package edu.ntudp.sau.spring_java.service.excel;

import edu.ntudp.sau.spring_java.model.dto.product.ProductParsingDto;
import edu.ntudp.sau.spring_java.model.dto.product.ProductResponseDto;

import java.util.List;

public interface ExcelReportGenerator {
    byte[] generateSearchReport(String search, List<ProductResponseDto> productReposnseDtos);
    byte[] generateDatabaseReport(List<ProductResponseDto> productReposnseDtos);
}