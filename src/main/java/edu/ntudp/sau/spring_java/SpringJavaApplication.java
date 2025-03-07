package edu.ntudp.sau.spring_java;

import edu.ntudp.sau.spring_java.service.RozetkaParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringJavaApplication {
	public static void main(String[] args) {
		var context = SpringApplication.run(SpringJavaApplication.class, args);
	}
}
