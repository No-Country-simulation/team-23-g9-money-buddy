package com.moneybuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MoneyBuddyApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyBuddyApplication.class, args);
	}
}
