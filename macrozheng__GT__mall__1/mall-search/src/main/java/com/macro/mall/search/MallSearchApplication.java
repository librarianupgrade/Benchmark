package com.macro.mall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.macro.mall")
public class MallSearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(MallSearchApplication.class, args);
	}
}
