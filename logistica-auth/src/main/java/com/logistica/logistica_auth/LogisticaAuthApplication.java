package com.logistica.logistica_auth;

import com.logistica.logistica_auth.adapter.out.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class LogisticaAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(LogisticaAuthApplication.class, args);
	}

}
