package com.dogukan.appointment.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.dogukan.appointment"})
@EnableJpaRepositories(basePackages = {"com.dogukan.appointment"})
@ComponentScan(basePackages = "com.dogukan.appointment")
@EntityScan(basePackages = "com.dogukan.appointment.model")
public class AppointmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppointmentApplication.class, args);
	}

}
