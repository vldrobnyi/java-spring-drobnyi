package edu.ntudp.sau.spring_java.controller;

import edu.ntudp.sau.spring_java.model.dto.product.ProductParsingDto;
import edu.ntudp.sau.spring_java.model.dto.product.ProductResponseDto;
import edu.ntudp.sau.spring_java.service.ProductService;
import edu.ntudp.sau.spring_java.service.parser.RozetkaParser;
import edu.ntudp.sau.spring_java.service.excel.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private RozetkaParser rozetkaParser;

    @Autowired
    private ExcelService excelService;

    @Autowired
    private ProductService productService;

    @GetMapping
    public String parseProducts(@RequestParam(name = "search") String search,
                                @RequestParam(name = "pageLimit", defaultValue = "1") int pageLimit,
                                Model model) {

        List<ProductParsingDto> productParsingDtos = rozetkaParser.parseProducts(search, pageLimit);
        if (productParsingDtos != null && !productParsingDtos.isEmpty()) {
            List<ProductResponseDto> savedProducts = productService.saveProducts(productParsingDtos);

            model.addAttribute("products", savedProducts);
        } else {
            model.addAttribute("error", "Something went wrong!");
        }

        return "products";
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> generateSearchExcelReport(@RequestParam(name = "search") String search,
                                                            @RequestParam(name = "pageLimit", defaultValue = "1") int pageLimit) {

        List<ProductParsingDto> productParsingDtos = rozetkaParser.parseProducts(search, pageLimit);

        if (productParsingDtos == null || productParsingDtos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        byte[] excelFile = excelService.generateSearchReport(search, productParsingDtos);

        if (excelFile == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename("products_report_" + new Date().getTime() + ".xlsx").build());

        return new ResponseEntity<>(excelFile, headers, HttpStatus.OK);
    }

    @GetMapping("/excel/db")
    public ResponseEntity<byte[]> generateDatabaseExcelReport() {

        List<ProductResponseDto> productResponseDtos = productService.getAllProducts();

        if (productResponseDtos == null || productResponseDtos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        byte[] excelFile = excelService.generateDatabaseReport(productResponseDtos);

        if (excelFile == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename("products_report_" + new Date().getTime() + ".xlsx").build());

        return new ResponseEntity<>(excelFile, headers, HttpStatus.OK);
    }
}
