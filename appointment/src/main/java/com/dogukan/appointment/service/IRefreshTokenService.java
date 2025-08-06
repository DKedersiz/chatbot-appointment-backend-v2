package com.dogukan.appointment.service;

import com.dogukan.appointment.model.RefreshToken;
import com.dogukan.appointment.model.User;

public interface IRefreshTokenService {
    public RefreshToken createRefreshToken(User user);
    public String generateNewAccessToken(String refreshToken);

    }
