package com.example.rest_tdd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class RestTddApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestTddApplication.class, args);
	}

}
