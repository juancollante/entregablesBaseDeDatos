package com.logistica.logistica_envios.adapter.out.persistence;

import com.logistica.logistica_envios.adapter.out.persistence.entity.AuditoriaEnvioEntity;
import com.logistica.logistica_envios.adapter.out.persistence.entity.DestinatarioEntity;
import com.logistica.logistica_envios.adapter.out.persistence.entity.DireccionEntity;
import com.logistica.logistica_envios.adapter.out.persistence.entity.EnvioEntity;
import com.logistica.logistica_envios.adapter.out.persistence.entity.EstadoEnvioEntity;
import com.logistica.logistica_envios.adapter.out.persistence.entity.RemitenteEntity;
import com.logistica.logistica_envios.adapter.out.persistence.repository.AuditoriaEnvioJpaRepository;
import com.logistica.logistica_envios.adapter.out.persistence.repository.DestinatarioJpaRepository;
import com.logistica.logistica_envios.adapter.out.persistence.repository.DireccionJpaRepository;
import com.logistica.logistica_envios.adapter.out.persistence.repository.EnvioJpaRepository;
import com.logistica.logistica_envios.adapter.out.persistence.repository.EstadoEnvioJpaRepository;
import com.logistica.logistica_envios.adapter.out.persistence.repository.RemitenteJpaRepository;
import com.logistica.logistica_envios.domain.model.NewShipment;
import com.logistica.logistica_envios.domain.model.PartyContact;
import com.logistica.logistica_envios.domain.model.PostalAddress;
import com.logistica.logistica_envios.domain.model.ShipmentDetail;
import com.logistica.logistica_envios.domain.port.out.ShipmentPersistencePort;
import com.logistica.logistica_envios.domain.port.out.TrackingNumberPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class ShipmentPersistenceAdapter implements ShipmentPersistencePort {

    private static final String ESTADO_INICIAL = "CREADO";
    private static final String DEPARTAMENTO = "Antioquia";
    private static final String PAIS = "Colombia";

    private final EstadoEnvioJpaRepository estadoEnvioJpaRepository;
    private final DireccionJpaRepository direccionJpaRepository;
    private final RemitenteJpaRepository remitenteJpaRepository;
    private final DestinatarioJpaRepository destinatarioJpaRepository;
    private final EnvioJpaRepository envioJpaRepository;
    private final AuditoriaEnvioJpaRepository auditoriaEnvioJpaRepository;
    private final TrackingNumberPort trackingNumberPort;

    public ShipmentPersistenceAdapter(
            EstadoEnvioJpaRepository estadoEnvioJpaRepository,
            DireccionJpaRepository direccionJpaRepository,
            RemitenteJpaRepository remitenteJpaRepository,
            DestinatarioJpaRepository destinatarioJpaRepository,
            EnvioJpaRepository envioJpaRepository,
            AuditoriaEnvioJpaRepository auditoriaEnvioJpaRepository,
            TrackingNumberPort trackingNumberPort
    ) {
        this.estadoEnvioJpaRepository = estadoEnvioJpaRepository;
        this.direccionJpaRepository = direccionJpaRepository;
        this.remitenteJpaRepository = remitenteJpaRepository;
        this.destinatarioJpaRepository = destinatarioJpaRepository;
        this.envioJpaRepository = envioJpaRepository;
        this.auditoriaEnvioJpaRepository = auditoriaEnvioJpaRepository;
        this.trackingNumberPort = trackingNumberPort;
    }

    @Override
    @Transactional
    public ShipmentDetail saveNew(NewShipment shipment, UUID creadoPorUsuarioId, String codigoSedeRegistro) {
        EstadoEnvioEntity estado = estadoEnvioJpaRepository.findByCodigo(ESTADO_INICIAL)
                .orElseThrow(() -> new IllegalStateException("Estado CREADO no configurado en BD"));

        String numeroGuia = null;
        for (int intento = 0; intento < 8; intento++) {
            String candidato = trackingNumberPort.nextNumeroGuia();
            if (!envioJpaRepository.existsByNumeroGuia(candidato)) {
                numeroGuia = candidato;
                break;
            }
        }
        if (numeroGuia == null) {
            throw new IllegalStateException("No se pudo generar numero_guia único");
        }

        Instant now = Instant.now();

        DireccionEntity dirRem = toDireccionEntity(shipment.remitente().direccion());
        DireccionEntity dirDest = toDireccionEntity(shipment.destinatario().direccion());
        direccionJpaRepository.save(dirRem);
        direccionJpaRepository.save(dirDest);

        RemitenteEntity rem = new RemitenteEntity();
        rem.setId(UUID.randomUUID());
        rem.setNombreCompleto(shipment.remitente().nombreCompleto());
        rem.setDocumento(shipment.remitente().documento());
        rem.setEmail(shipment.remitente().email());
        rem.setTelefono(shipment.remitente().telefono());
        rem.setDireccion(dirRem);
        remitenteJpaRepository.save(rem);

        DestinatarioEntity dest = new DestinatarioEntity();
        dest.setId(UUID.randomUUID());
        dest.setNombreCompleto(shipment.destinatario().nombreCompleto());
        dest.setDocumento(shipment.destinatario().documento());
        dest.setEmail(shipment.destinatario().email());
        dest.setTelefono(shipment.destinatario().telefono());
        dest.setDireccion(dirDest);
        destinatarioJpaRepository.save(dest);

        EnvioEntity envio = new EnvioEntity();
        envio.setId(UUID.randomUUID());
        envio.setNumeroGuia(numeroGuia);
        envio.setEstadoEnvio(estado);
        envio.setRemitente(rem);
        envio.setDestinatario(dest);
        envio.setDescripcionPaquete(shipment.descripcionPaquete());
        envio.setPesoKg(shipment.pesoKg());
        envio.setFechaCreacion(now);
        envio.setFechaEstimadaEntrega(shipment.fechaEstimadaEntrega());
        envio.setCodigoSedeRegistro(codigoSedeRegistro);
        envio.setCreadoPorUsuarioIdExterno(creadoPorUsuarioId);
        envio.setUpdatedAt(now);
        envioJpaRepository.save(envio);

        AuditoriaEnvioEntity audit = new AuditoriaEnvioEntity();
        audit.setId(UUID.randomUUID());
        audit.setEnvio(envio);
        audit.setAccion("CREAR");
        audit.setUsuarioIdExterno(creadoPorUsuarioId);
        audit.setValoresAnteriores(null);
        Map<String, Object> nuevos = new HashMap<>();
        nuevos.put("numeroGuia", numeroGuia);
        nuevos.put("estadoCodigo", ESTADO_INICIAL);
        nuevos.put("codigoSedeRegistro", codigoSedeRegistro);
        audit.setValoresNuevos(nuevos);
        audit.setCreatedAt(now);
        auditoriaEnvioJpaRepository.save(audit);

        return toDetail(envio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShipmentDetail> findByNumeroGuia(String numeroGuia) {
        return envioJpaRepository.findDetailedByNumeroGuia(numeroGuia).map(this::toDetail);
    }

    private DireccionEntity toDireccionEntity(PostalAddress a) {
        DireccionEntity e = new DireccionEntity();
        e.setId(UUID.randomUUID());
        e.setLinea1(a.linea1());
        e.setLinea2(a.linea2());
        e.setMunicipioCodigoDane(a.municipioCodigoDane());
        e.setMunicipioNombre(a.municipioNombre());
        e.setDepartamento(DEPARTAMENTO);
        e.setPais(PAIS);
        e.setCodigoPostal(a.codigoPostal());
        e.setReferencias(a.referencias());
        return e;
    }

    private ShipmentDetail toDetail(EnvioEntity e) {
        return new ShipmentDetail(
                e.getId(),
                e.getNumeroGuia(),
                e.getEstadoEnvio().getCodigo(),
                e.getEstadoEnvio().getNombre(),
                e.getFechaCreacion(),
                e.getFechaEstimadaEntrega(),
                e.getCodigoSedeRegistro(),
                e.getCreadoPorUsuarioIdExterno(),
                e.getDescripcionPaquete(),
                e.getPesoKg(),
                toParty(e.getRemitente()),
                toParty(e.getDestinatario())
        );
    }

    private PartyContact toParty(RemitenteEntity r) {
        return new PartyContact(
                r.getNombreCompleto(),
                r.getDocumento(),
                r.getEmail(),
                r.getTelefono(),
                toPostal(r.getDireccion())
        );
    }

    private PartyContact toParty(DestinatarioEntity d) {
        return new PartyContact(
                d.getNombreCompleto(),
                d.getDocumento(),
                d.getEmail(),
                d.getTelefono(),
                toPostal(d.getDireccion())
        );
    }

    private PostalAddress toPostal(DireccionEntity d) {
        return new PostalAddress(
                d.getLinea1(),
                d.getLinea2(),
                d.getMunicipioCodigoDane(),
                d.getMunicipioNombre(),
                d.getDepartamento(),
                d.getPais(),
                d.getCodigoPostal(),
                d.getReferencias()
        );
    }
}
