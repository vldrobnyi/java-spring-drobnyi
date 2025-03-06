package edu.ntudp.sau.spring_java;

import edu.ntudp.sau.spring_java.service.ProductParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringJavaApplication {

	public static void main(String[] args) {
		var context = SpringApplication.run(SpringJavaApplication.class, args);
		ProductParser parser = context.getBean(ProductParser.class);

		parser.parseProducts("ps 5", 2);
	}

}
