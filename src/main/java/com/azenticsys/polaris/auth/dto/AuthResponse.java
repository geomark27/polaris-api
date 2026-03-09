package com.azenticsys.polaris.auth.dto;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String username,
        String tenantSlug,
        String accessToken,
        String refreshToken
) {}
