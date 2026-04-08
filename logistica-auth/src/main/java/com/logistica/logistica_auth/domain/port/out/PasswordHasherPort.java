package com.logistica.logistica_auth.domain.port.out;

public interface PasswordHasherPort {

    boolean matches(String rawPassword, String encodedHash);

    String encode(String rawPassword);
}
