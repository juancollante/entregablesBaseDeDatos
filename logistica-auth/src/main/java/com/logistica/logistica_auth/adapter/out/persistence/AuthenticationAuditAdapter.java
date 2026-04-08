package com.logistica.logistica_auth.adapter.out.persistence;

import com.logistica.logistica_auth.adapter.out.persistence.entity.AuditoriaAutenticacionEntity;
import com.logistica.logistica_auth.adapter.out.persistence.repository.AuditoriaAutenticacionJpaRepository;
import com.logistica.logistica_auth.domain.port.out.AuthenticationAuditPort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class AuthenticationAuditAdapter implements AuthenticationAuditPort {

    private final AuditoriaAutenticacionJpaRepository repository;

    public AuthenticationAuditAdapter(AuditoriaAutenticacionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void record(UUID userIdOrNull, String tipoEvento, String ip, String userAgent, String detalle) {
        AuditoriaAutenticacionEntity row = new AuditoriaAutenticacionEntity();
        row.setId(UUID.randomUUID());
        row.setUsuarioId(userIdOrNull);
        row.setTipoEvento(tipoEvento);
        row.setIp(truncate(ip, 64));
        row.setUserAgent(truncate(userAgent, 512));
        row.setDetalle(truncate(detalle, 1000));
        row.setCreatedAt(Instant.now());
        repository.save(row);
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
