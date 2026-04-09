package com.logistica.logistica_envios.adapter.in.web;

import com.logistica.logistica_envios.adapter.in.web.dto.CreateEnvioRequest;
import com.logistica.logistica_envios.adapter.in.web.dto.ShipmentPublicResponse;
import com.logistica.logistica_envios.adapter.in.web.dto.ShipmentResponse;
import com.logistica.logistica_envios.adapter.in.web.security.JwtUserPrincipal;
import com.logistica.logistica_envios.domain.model.NewShipment;
import com.logistica.logistica_envios.domain.model.PartyContact;
import com.logistica.logistica_envios.domain.model.PostalAddress;
import com.logistica.logistica_envios.domain.port.in.GetShipmentByTrackingUseCase;
import com.logistica.logistica_envios.domain.port.in.RegisterShipmentUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/envios")
public class EnvioController {

    private static final String DEPARTAMENTO = "Antioquia";
    private static final String PAIS = "Colombia";

    private final RegisterShipmentUseCase registerShipmentUseCase;
    private final GetShipmentByTrackingUseCase getShipmentByTrackingUseCase;

    public EnvioController(
            RegisterShipmentUseCase registerShipmentUseCase,
            GetShipmentByTrackingUseCase getShipmentByTrackingUseCase
    ) {
        this.registerShipmentUseCase = registerShipmentUseCase;
        this.getShipmentByTrackingUseCase = getShipmentByTrackingUseCase;
    }

    @PostMapping
    public ResponseEntity<ShipmentResponse> crear(
            @Valid @RequestBody CreateEnvioRequest body,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        NewShipment cmd = toNewShipment(body);
        var saved = registerShipmentUseCase.register(cmd, principal.id(), principal.codigoSedeAsignada());
        return ResponseEntity.status(HttpStatus.CREATED).body(ShipmentResponse.from(saved));
    }

    /**
     * Consulta pública: solo estado, fechas y municipios de origen/destino (sin datos personales).
     */
    @GetMapping("/public/{numeroGuia}")
    public ResponseEntity<ShipmentPublicResponse> obtenerPublico(@PathVariable String numeroGuia) {
        var detail = getShipmentByTrackingUseCase.getByNumeroGuia(numeroGuia);
        return ResponseEntity.ok(ShipmentPublicResponse.from(detail));
    }

    /**
     * Detalle completo del envío; requiere JWT (cualquier rol autenticado).
     */
    @GetMapping("/{numeroGuia}")
    public ResponseEntity<ShipmentResponse> obtener(@PathVariable String numeroGuia) {
        var detail = getShipmentByTrackingUseCase.getByNumeroGuia(numeroGuia);
        return ResponseEntity.ok(ShipmentResponse.from(detail));
    }

    private static NewShipment toNewShipment(CreateEnvioRequest body) {
        return new NewShipment(
                toParty(body.remitente()),
                toParty(body.destinatario()),
                body.descripcionPaquete(),
                body.pesoKg(),
                body.fechaEstimadaEntrega(),
                body.codigoSedeRegistro()
        );
    }

    private static PartyContact toParty(com.logistica.logistica_envios.adapter.in.web.dto.PartyRequest r) {
        var d = r.direccion();
        PostalAddress addr = new PostalAddress(
                d.linea1(),
                d.linea2(),
                d.municipioCodigoDane(),
                d.municipioNombre(),
                DEPARTAMENTO,
                PAIS,
                d.codigoPostal(),
                d.referencias()
        );
        return new PartyContact(
                r.nombreCompleto(),
                r.documento(),
                r.email(),
                r.telefono(),
                addr
        );
    }
}
