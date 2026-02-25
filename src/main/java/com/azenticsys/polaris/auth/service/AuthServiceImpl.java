package com.azenticsys.polaris.auth.service;

import com.azenticsys.polaris.auth.dto.AuthResponse;
import com.azenticsys.polaris.auth.dto.LoginRequest;
import com.azenticsys.polaris.auth.dto.RefreshTokenRequest;
import com.azenticsys.polaris.auth.entity.RevokedToken;
import com.azenticsys.polaris.auth.repository.RevokedTokenRepository;
import com.azenticsys.polaris.config.JwtService;
import com.azenticsys.polaris.user.entity.User;
import com.azenticsys.polaris.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RevokedTokenRepository revokedTokenRepository;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameAndDeletedAtIsNull(request.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!user.isActive()) {
            throw new BadCredentialsException("Account is disabled");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return buildAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest request) {
        String token = request.refreshToken();

        if (!jwtService.isTokenValid(token) || jwtService.isAccessToken(token)) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!user.isActive()) {
            throw new BadCredentialsException("Account is disabled");
        }

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public void logout(String accessToken) {
        if (!jwtService.isTokenValid(accessToken) || !jwtService.isAccessToken(accessToken)) {
            throw new BadCredentialsException("Invalid token");
        }

        String jti = jwtService.extractJti(accessToken);

        if (!revokedTokenRepository.existsByJti(jti)) {
            revokedTokenRepository.save(
                    new RevokedToken(jti, jwtService.extractExpirationAsLocalDateTime(accessToken))
            );
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken  = jwtService.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getUsername());
        return new AuthResponse(user.getId(), user.getUsername(), accessToken, refreshToken);
    }
}
