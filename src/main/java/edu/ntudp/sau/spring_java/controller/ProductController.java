package edu.ntudp.sau.spring_java.controller;

import edu.ntudp.sau.spring_java.model.dto.ProductDto;
import edu.ntudp.sau.spring_java.service.RozetkaParser;
import edu.ntudp.sau.spring_java.service.excel.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ProductController {

    @Autowired
    private RozetkaParser rozetkaParser;

    @Autowired
    private ExcelService excelService;

    @GetMapping("/products")
    public String parseProducts(@RequestParam(name = "search") String search,
                                @RequestParam(name = "pageLimit", defaultValue = "1") int pageLimit,
                                Model model) {

        List<ProductDto> products = rozetkaParser.parseProducts(search, pageLimit);
        model.addAttribute("products", products);

        return "products";
    }

    @GetMapping("/products/excel")
    public ResponseEntity<byte[]> generateExcelReport(@RequestParam(name = "search") String search,
                                                      @RequestParam(name = "pageLimit", defaultValue = "1") int pageLimit) {

        List<ProductDto> productDtos = rozetkaParser.parseProducts(search, pageLimit);

        if (productDtos == null || productDtos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        byte[] excelFile = excelService.generateSearchReport(search, productDtos);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename("products_report.xlsx").build());

        return new ResponseEntity<>(excelFile, headers, HttpStatus.OK);
    }
}
