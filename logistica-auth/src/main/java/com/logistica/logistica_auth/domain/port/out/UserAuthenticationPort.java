package com.logistica.logistica_auth.domain.port.out;

import com.logistica.logistica_auth.domain.model.UserAccount;

import java.util.Optional;
import java.util.UUID;

public interface UserAuthenticationPort {

    Optional<UserAccount> findActiveByEmail(String email);

    Optional<UserAccount> findActiveById(UUID id);
}
