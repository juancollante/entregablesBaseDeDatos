package com.logistica.logistica_auth.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank String refreshToken
) {
}
