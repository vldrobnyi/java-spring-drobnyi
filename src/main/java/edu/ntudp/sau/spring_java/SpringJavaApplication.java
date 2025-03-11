package edu.ntudp.sau.spring_java;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "edu.ntudp.sau.spring_java.model.entity")
public class SpringJavaApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringJavaApplication.class, args);
	}
}
