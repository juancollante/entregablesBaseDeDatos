package com.logistica.logistica_auth.adapter.out.security;

import com.logistica.logistica_auth.domain.port.out.PasswordHasherPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptPasswordHasherAdapter implements PasswordHasherPort {

    private final PasswordEncoder passwordEncoder;

    public BcryptPasswordHasherAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean matches(String rawPassword, String encodedHash) {
        return passwordEncoder.matches(rawPassword, encodedHash);
    }

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
