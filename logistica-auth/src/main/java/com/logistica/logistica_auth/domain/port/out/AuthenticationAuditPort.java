package com.logistica.logistica_auth.domain.port.out;

import java.util.UUID;

public interface AuthenticationAuditPort {

    void record(UUID userIdOrNull, String tipoEvento, String ip, String userAgent, String detalle);
}
