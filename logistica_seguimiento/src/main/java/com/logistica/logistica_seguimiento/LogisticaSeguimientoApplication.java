package com.logistica.logistica_seguimiento;

import com.logistica.logistica_seguimiento.adapter.in.web.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class LogisticaSeguimientoApplication {

	public static void main(String[] args) {
		SpringApplication.run(LogisticaSeguimientoApplication.class, args);
	}

}
