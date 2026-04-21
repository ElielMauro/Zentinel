package com.zentinel.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.Locale;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		Locale.setDefault(new Locale("es", "MX"));
		SpringApplication.run(DemoApplication.class, args);
	}

}
