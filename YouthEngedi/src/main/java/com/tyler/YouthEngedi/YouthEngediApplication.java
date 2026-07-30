package com.tyler.YouthEngedi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class YouthEngediApplication {

	public static void main(String[] args) {
		SpringApplication.run(YouthEngediApplication.class, args);
	}

}
