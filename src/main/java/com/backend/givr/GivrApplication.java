package com.backend.givr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.Ordered;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry(order = Ordered.LOWEST_PRECEDENCE)
public class GivrApplication {

	public static void main(String[] args) {
		SpringApplication.run(GivrApplication.class, args);
	}

}
