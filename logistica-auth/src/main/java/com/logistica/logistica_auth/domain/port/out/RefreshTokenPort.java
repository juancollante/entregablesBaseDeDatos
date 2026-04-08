package com.logistica.logistica_auth.domain.port.out;

import com.logistica.logistica_auth.domain.model.UserAccount;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenPort {

    record StoredRefresh(String jwt, UUID recordId) {}

    StoredRefresh issue(UserAccount user);

    Optional<UserAccount> validateAndConsume(String refreshJwt);
}
