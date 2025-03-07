package edu.ntudp.sau.spring_java.controller;

import edu.ntudp.sau.spring_java.model.Product;
import edu.ntudp.sau.spring_java.service.RozetkaParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ProductController {

    @Autowired
    private RozetkaParser rozetkaParser;

    @GetMapping("/products")
    public String parseProducts(@RequestParam(name = "search") String search,
                                @RequestParam(name = "pageLimit", defaultValue = "1") int pageLimit,
                                @RequestParam(name = "productsLimit", defaultValue = "10") int productsLimit,
                                Model model) {

        List<Product> products = rozetkaParser.parseProducts(search, pageLimit, productsLimit);
        model.addAttribute("products", products);

        return "products"; // Return the Thymeleaf template name (products.html)
    }
}
