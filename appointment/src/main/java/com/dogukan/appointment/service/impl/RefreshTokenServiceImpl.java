package com.dogukan.appointment.service.impl;

import com.dogukan.appointment.jwt.JwtService;
import com.dogukan.appointment.model.RefreshToken;
import com.dogukan.appointment.model.User;
import com.dogukan.appointment.repository.RefreshTokenRepository;
import com.dogukan.appointment.service.IRefreshTokenService;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements IRefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;


    @Value("${JWT_REFRESH_TOKEN_EXPIRATION}")
    private Long refreshTokenExpirationMs;


    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository, JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .refreshToken(UUID.randomUUID().toString())
                .expiredDate(Date.from(Instant.now().plusMillis(refreshTokenExpirationMs)))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public String generateNewAccessToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if(token.getExpiredDate().before(new Date())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired");
        }

        User user = token.getUser();
        return jwtService.generateToken(user);

    }

}
