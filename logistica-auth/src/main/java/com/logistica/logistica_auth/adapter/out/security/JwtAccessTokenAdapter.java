package com.logistica.logistica_auth.adapter.out.security;

import com.logistica.logistica_auth.domain.model.UserAccount;
import com.logistica.logistica_auth.domain.port.out.AccessTokenPort;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtAccessTokenAdapter implements AccessTokenPort {

    public static final String CLAIM_TYPE = "typ";
    public static final String TYPE_ACCESS = "access";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_SEDE = "sede";

    private final SecretKey key;
    private final int accessTokenMinutes;

    public JwtAccessTokenAdapter(JwtProperties jwtProperties) {
        this.key = JwtSigningKey.fromConfig(jwtProperties.getSecret());
        this.accessTokenMinutes = jwtProperties.getAccessTokenMinutes();
    }

    @Override
    public String createAccessToken(UserAccount user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTokenMinutes * 60L);
        var builder = Jwts.builder()
                .subject(user.id().toString())
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .claim(CLAIM_ROLE, user.roleCode())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("email", user.email());
        if (user.codigoSedeAsignada() != null) {
            builder.claim(CLAIM_SEDE, user.codigoSedeAsignada());
        }
        return builder.signWith(key).compact();
    }
}
