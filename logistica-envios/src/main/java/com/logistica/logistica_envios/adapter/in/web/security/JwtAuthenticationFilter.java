package com.logistica.logistica_envios.adapter.in.web.security;

import com.logistica.logistica_envios.adapter.out.security.JwtAccessTokenClaims;
import com.logistica.logistica_envios.adapter.out.security.JwtProperties;
import com.logistica.logistica_envios.adapter.out.security.JwtSigningKey;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecretKey key;

    public JwtAuthenticationFilter(JwtProperties jwtProperties) {
        this.key = JwtSigningKey.fromConfig(jwtProperties.getSecret());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || header.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }
        header = header.trim();
        if (header.length() < 8 || !header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(7).trim();
        while (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = token.substring(7).trim();
        }
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            if (!JwtAccessTokenClaims.TYPE_ACCESS.equals(claims.get(JwtAccessTokenClaims.CLAIM_TYPE, String.class))) {
                filterChain.doFilter(request, response);
                return;
            }
            UUID id = UUID.fromString(claims.getSubject());
            String email = claims.get("email", String.class);
            String role = claims.get(JwtAccessTokenClaims.CLAIM_ROLE, String.class);
            String sede = claims.get(JwtAccessTokenClaims.CLAIM_SEDE, String.class);
            JwtUserPrincipal principal = new JwtUserPrincipal(id, email, role, sede);
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
            var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (JwtException | IllegalArgumentException ignored) {
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
}
