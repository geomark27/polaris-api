package com.azenticsys.polaris.auth.service;

import com.azenticsys.polaris.auth.dto.AuthResponse;
import com.azenticsys.polaris.auth.dto.LoginRequest;
import com.azenticsys.polaris.auth.dto.RefreshTokenRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(String accessToken);
}
