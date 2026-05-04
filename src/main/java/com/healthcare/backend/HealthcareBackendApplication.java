package com.healthcare.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HealthcareBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(HealthcareBackendApplication.class, args);
	}

}
