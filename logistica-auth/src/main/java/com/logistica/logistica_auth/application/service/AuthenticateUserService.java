package com.logistica.logistica_auth.application.service;

import com.logistica.logistica_auth.domain.model.AuthTokens;
import com.logistica.logistica_auth.domain.model.UserAccount;
import com.logistica.logistica_auth.domain.port.in.AuthenticateUserUseCase;
import com.logistica.logistica_auth.domain.exception.AuthCredentialsException;
import com.logistica.logistica_auth.domain.port.out.AccessTokenPort;
import com.logistica.logistica_auth.domain.port.out.AccessTokenTtlPort;
import com.logistica.logistica_auth.domain.port.out.AuthenticationAuditPort;
import com.logistica.logistica_auth.domain.port.out.PasswordHasherPort;
import com.logistica.logistica_auth.domain.port.out.RefreshTokenPort;
import com.logistica.logistica_auth.domain.port.out.UserAuthenticationPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticateUserService implements AuthenticateUserUseCase {

    private final UserAuthenticationPort userAuthenticationPort;
    private final PasswordHasherPort passwordHasherPort;
    private final AccessTokenPort accessTokenPort;
    private final RefreshTokenPort refreshTokenPort;
    private final AuthenticationAuditPort authenticationAuditPort;
    private final AccessTokenTtlPort accessTokenTtlPort;

    public AuthenticateUserService(
            UserAuthenticationPort userAuthenticationPort,
            PasswordHasherPort passwordHasherPort,
            AccessTokenPort accessTokenPort,
            RefreshTokenPort refreshTokenPort,
            AuthenticationAuditPort authenticationAuditPort,
            AccessTokenTtlPort accessTokenTtlPort
    ) {
        this.userAuthenticationPort = userAuthenticationPort;
        this.passwordHasherPort = passwordHasherPort;
        this.accessTokenPort = accessTokenPort;
        this.refreshTokenPort = refreshTokenPort;
        this.authenticationAuditPort = authenticationAuditPort;
        this.accessTokenTtlPort = accessTokenTtlPort;
    }

    @Override
    @Transactional
    public AuthTokens login(String email, String rawPassword, String ip, String userAgent) {
        var userOpt = userAuthenticationPort.findActiveByEmail(email.trim().toLowerCase());
        if (userOpt.isEmpty()) {
            authenticationAuditPort.record(null, "login_fail", ip, userAgent, "Usuario no encontrado");
            throw new AuthCredentialsException("Credenciales inválidas");
        }
        UserAccount user = userOpt.get();
        if (!passwordHasherPort.matches(rawPassword, user.passwordHash())) {
            authenticationAuditPort.record(user.id(), "login_fail", ip, userAgent, "Contraseña incorrecta");
            throw new AuthCredentialsException("Credenciales inválidas");
        }
        authenticationAuditPort.record(user.id(), "login_ok", ip, userAgent, null);
        String access = accessTokenPort.createAccessToken(user);
        var refresh = refreshTokenPort.issue(user);
        return new AuthTokens(access, refresh.jwt(), accessTokenTtlPort.expiresInSeconds());
    }

    @Override
    @Transactional
    public AuthTokens refresh(String refreshTokenJwt, String ip, String userAgent) {
        UserAccount user = refreshTokenPort.validateAndConsume(refreshTokenJwt)
                .orElseThrow(() -> new AuthCredentialsException("Refresh token inválido o revocado"));
        String access = accessTokenPort.createAccessToken(user);
        var refresh = refreshTokenPort.issue(user);
        authenticationAuditPort.record(user.id(), "refresh", ip, userAgent, null);
        return new AuthTokens(access, refresh.jwt(), accessTokenTtlPort.expiresInSeconds());
    }
}
