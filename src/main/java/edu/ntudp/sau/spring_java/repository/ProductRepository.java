package edu.ntudp.sau.spring_java.repository;

import edu.ntudp.sau.spring_java.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
