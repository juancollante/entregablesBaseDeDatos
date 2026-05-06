package com.logistica.logistica_notificaciones.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Sistema", description = "Comprobaciones de disponibilidad del servicio.")
public class HealthController {

    @GetMapping("/healthz")
    @Operation(summary = "Salud del servicio", description = "Indica si el proceso responde correctamente.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Servicio operativo")})
    public Map<String, String> healthz() {
        return Map.of("status", "ok");
    }
}
