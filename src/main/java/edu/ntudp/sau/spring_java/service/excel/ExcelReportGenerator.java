package edu.ntudp.sau.spring_java.service.excel;

import edu.ntudp.sau.spring_java.model.dto.ProductDto;

import java.util.List;

public interface ExcelReportGenerator {
    byte[] generateSearchReport(String search, List<ProductDto> productDtos);
}