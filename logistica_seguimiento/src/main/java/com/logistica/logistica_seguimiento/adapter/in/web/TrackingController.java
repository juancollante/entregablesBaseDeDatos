package com.logistica.logistica_seguimiento.adapter.in.web;

import com.logistica.logistica_seguimiento.adapter.in.web.dto.RegisterTrackingEventRequest;
import com.logistica.logistica_seguimiento.adapter.in.web.security.JwtUserPrincipal;
import com.logistica.logistica_seguimiento.config.OpenApiDocSupport;
import com.logistica.logistica_seguimiento.domain.model.SedeOption;
import com.logistica.logistica_seguimiento.domain.model.TrackingEvent;
import com.logistica.logistica_seguimiento.domain.port.in.GetTrackingHistoryUseCase;
import com.logistica.logistica_seguimiento.domain.port.in.ListSedesUseCase;
import com.logistica.logistica_seguimiento.domain.port.in.RegisterTrackingEventUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tracking")
@Tag(name = "Seguimiento", description = "Historial por guía, sedes y registro de eventos logísticos.")
public class TrackingController {

    private final RegisterTrackingEventUseCase registerTrackingEventUseCase;
    private final GetTrackingHistoryUseCase getTrackingHistoryUseCase;
    private final ListSedesUseCase listSedesUseCase;

    public TrackingController(
            RegisterTrackingEventUseCase registerTrackingEventUseCase,
            GetTrackingHistoryUseCase getTrackingHistoryUseCase,
            ListSedesUseCase listSedesUseCase
    ) {
        this.registerTrackingEventUseCase = registerTrackingEventUseCase;
        this.getTrackingHistoryUseCase = getTrackingHistoryUseCase;
        this.listSedesUseCase = listSedesUseCase;
    }

    @GetMapping("/sedes")
    @SecurityRequirement(name = OpenApiDocSupport.JWT_SCHEME)
    @Operation(summary = "Listar sedes activas", description = "Catálogo para formularios de evento. Requiere JWT (OPERADOR/ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<List<SedeOption>> listarSedes(@AuthenticationPrincipal JwtUserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(listSedesUseCase.listActivas());
    }

    @PostMapping("/eventos")
    @SecurityRequirement(name = OpenApiDocSupport.JWT_SCHEME)
    @Operation(summary = "Registrar evento de seguimiento", description = "Asocia un hito a una guía. Requiere JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento registrado"),
            @ApiResponse(responseCode = "400", description = "Validación o reglas de negocio"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "500", description = "Error de persistencia u otro error no previsto")
    })
    public ResponseEntity<TrackingEvent> registrar(
            @Valid @RequestBody RegisterTrackingEventRequest body,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        TrackingEvent created = registerTrackingEventUseCase.register(
                body.numeroGuia(),
                body.tipoEventoCodigo(),
                body.sedeCodigo(),
                body.fechaHora(),
                body.descripcion(),
                body.metadata(),
                principal.id()
        );
        return ResponseEntity.ok(created);
    }

    @GetMapping("/historial/{numeroGuia}")
    @Operation(
            summary = "Historial por guía",
            description = "Lista cronológica de eventos. Consulta pública; no requiere JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de eventos (vacío si aún no hay eventos para la guía)"),
            @ApiResponse(responseCode = "400", description = "Número de guía inválido")
    })
    public ResponseEntity<List<TrackingEvent>> historial(
            @Parameter(description = "Número de guía del envío", example = "LT-2026-000001")
            @PathVariable("numeroGuia") String numeroGuia) {
        return ResponseEntity.ok(getTrackingHistoryUseCase.getHistory(numeroGuia));
    }
}
