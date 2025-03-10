package edu.ntudp.sau.spring_java.repository;

import edu.ntudp.sau.spring_java.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
