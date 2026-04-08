package com.logistica.logistica_auth.domain.port.in;

import com.logistica.logistica_auth.domain.model.AuthTokens;

public interface AuthenticateUserUseCase {

    AuthTokens login(String email, String rawPassword, String ip, String userAgent);

    AuthTokens refresh(String refreshTokenJwt, String ip, String userAgent);
}
