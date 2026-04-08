package com.logistica.logistica_auth;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Carga completa del contexto requiere PostgreSQL y migraciones Flyway en ejecución")
class LogisticaAuthApplicationTests {

    @Test
    void contextLoads() {
    }
}
