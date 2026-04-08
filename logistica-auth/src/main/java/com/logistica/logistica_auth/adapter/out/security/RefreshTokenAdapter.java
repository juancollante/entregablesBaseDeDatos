package com.logistica.logistica_auth.adapter.out.security;

import com.logistica.logistica_auth.adapter.out.persistence.entity.RefreshTokenEntity;
import com.logistica.logistica_auth.adapter.out.persistence.repository.RefreshTokenJpaRepository;
import com.logistica.logistica_auth.domain.model.UserAccount;
import com.logistica.logistica_auth.domain.port.out.RefreshTokenPort;
import com.logistica.logistica_auth.domain.port.out.UserAuthenticationPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
@Transactional
public class RefreshTokenAdapter implements RefreshTokenPort {

    public static final String CLAIM_TYPE = "typ";
    public static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final int refreshTokenDays;
    private final RefreshTokenJpaRepository refreshTokenJpaRepository;
    private final UserAuthenticationPort userAuthenticationPort;

    public RefreshTokenAdapter(
            JwtProperties jwtProperties,
            RefreshTokenJpaRepository refreshTokenJpaRepository,
            UserAuthenticationPort userAuthenticationPort
    ) {
        this.key = JwtSigningKey.fromConfig(jwtProperties.getSecret());
        this.refreshTokenDays = jwtProperties.getRefreshTokenDays();
        this.refreshTokenJpaRepository = refreshTokenJpaRepository;
        this.userAuthenticationPort = userAuthenticationPort;
    }

    @Override
    public StoredRefresh issue(UserAccount user) {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Instant exp = now.plus(refreshTokenDays, ChronoUnit.DAYS);
        String jwt = Jwts.builder()
                .id(id.toString())
                .subject(user.id().toString())
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
        String hash = TokenHashUtils.sha256Hex(jwt);
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId(id);
        entity.setUsuarioId(user.id());
        entity.setTokenHash(hash);
        entity.setExpiresAt(exp);
        entity.setRevocada(false);
        entity.setCreatedAt(now);
        refreshTokenJpaRepository.save(entity);
        return new StoredRefresh(jwt, id);
    }

    @Override
    public Optional<UserAccount> validateAndConsume(String refreshJwt) {
        try {
            var parsed = Jwts.parser().verifyWith(key).build().parseSignedClaims(refreshJwt);
            Claims claims = parsed.getPayload();
            if (!TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class))) {
                return Optional.empty();
            }
            UUID tokenId = UUID.fromString(claims.getId());
            UUID subjectUserId = UUID.fromString(claims.getSubject());
            return refreshTokenJpaRepository.findById(tokenId).flatMap(entity -> {
                if (entity.isRevocada() || Instant.now().isAfter(entity.getExpiresAt())) {
                    return Optional.empty();
                }
                if (!entity.getUsuarioId().equals(subjectUserId)) {
                    return Optional.empty();
                }
                String incomingHash = TokenHashUtils.sha256Hex(refreshJwt);
                if (!MessageDigestEquals.constantTimeEquals(incomingHash, entity.getTokenHash())) {
                    return Optional.empty();
                }
                entity.setRevocada(true);
                refreshTokenJpaRepository.save(entity);
                return userAuthenticationPort.findActiveById(subjectUserId);
            });
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static final class MessageDigestEquals {
        static boolean constantTimeEquals(String a, String b) {
            if (a == null || b == null || a.length() != b.length()) {
                return false;
            }
            int r = 0;
            for (int i = 0; i < a.length(); i++) {
                r |= a.charAt(i) ^ b.charAt(i);
            }
            return r == 0;
        }
    }
}
