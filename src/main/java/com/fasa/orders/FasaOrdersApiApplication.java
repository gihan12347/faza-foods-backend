package com.fasa.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FasaOrdersApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FasaOrdersApiApplication.class, args);
	}

}
