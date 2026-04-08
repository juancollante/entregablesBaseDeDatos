package com.logistica.logistica_auth.adapter.in.web;

import com.logistica.logistica_auth.adapter.in.web.dto.LoginRequest;
import com.logistica.logistica_auth.adapter.in.web.dto.MeResponse;
import com.logistica.logistica_auth.adapter.in.web.dto.RefreshRequest;
import com.logistica.logistica_auth.adapter.in.web.dto.TokenResponse;
import com.logistica.logistica_auth.adapter.in.web.security.JwtUserPrincipal;
import com.logistica.logistica_auth.domain.port.in.AuthenticateUserUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticateUserUseCase authenticateUserUseCase;

    public AuthController(AuthenticateUserUseCase authenticateUserUseCase) {
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest body,
            HttpServletRequest request
    ) {
        var tokens = authenticateUserUseCase.login(
                body.email(),
                body.password(),
                clientIp(request),
                request.getHeader("User-Agent")
        );
        return ResponseEntity.ok(TokenResponse.from(tokens.accessToken(), tokens.refreshToken(), tokens.expiresInSeconds()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody RefreshRequest body,
            HttpServletRequest request
    ) {
        var tokens = authenticateUserUseCase.refresh(
                body.refreshToken(),
                clientIp(request),
                request.getHeader("User-Agent")
        );
        return ResponseEntity.ok(TokenResponse.from(tokens.accessToken(), tokens.refreshToken(), tokens.expiresInSeconds()));
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal JwtUserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(new MeResponse(
                principal.id(),
                principal.email(),
                principal.roleCode(),
                principal.codigoSedeAsignada()
        ));
    }

    private static String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
