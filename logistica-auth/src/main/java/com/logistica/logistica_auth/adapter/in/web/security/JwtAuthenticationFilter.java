package com.logistica.logistica_auth.adapter.in.web.security;

import com.logistica.logistica_auth.adapter.out.security.JwtAccessTokenAdapter;
import com.logistica.logistica_auth.adapter.out.security.JwtProperties;
import com.logistica.logistica_auth.adapter.out.security.JwtSigningKey;
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
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return pathWithoutContext(request).equals("/api/auth/login")
                || pathWithoutContext(request).equals("/api/auth/refresh");
    }

    private static String pathWithoutContext(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            uri = uri.substring(ctx.length());
        }
        int q = uri.indexOf('?');
        if (q >= 0) {
            uri = uri.substring(0, q);
        }
        return uri.isEmpty() ? "/" : uri;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(7).trim();
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            if (!JwtAccessTokenAdapter.TYPE_ACCESS.equals(claims.get(JwtAccessTokenAdapter.CLAIM_TYPE, String.class))) {
                filterChain.doFilter(request, response);
                return;
            }
            UUID id = UUID.fromString(claims.getSubject());
            String email = claims.get("email", String.class);
            String role = claims.get(JwtAccessTokenAdapter.CLAIM_ROLE, String.class);
            String sede = claims.get(JwtAccessTokenAdapter.CLAIM_SEDE, String.class);
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
