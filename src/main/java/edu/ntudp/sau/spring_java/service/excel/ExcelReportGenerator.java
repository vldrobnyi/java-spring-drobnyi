package edu.ntudp.sau.spring_java.service.excel;

import edu.ntudp.sau.spring_java.model.Product;

import java.util.List;

public interface ExcelReportGenerator {
    byte[] generateSearchReport(String search, List<Product> products);
}