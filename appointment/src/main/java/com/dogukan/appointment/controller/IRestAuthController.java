package com.dogukan.appointment.controller;

import com.dogukan.appointment.dto.*;
import com.dogukan.appointment.model.RootEntity;

public interface IRestAuthController {
    public RootEntity<String> register(DtoRegisterRequest request);
    public RootEntity<DtoLoginResponse> login(DtoLoginRequest request);
    public RootEntity<RefreshTokenResponse> refreshToken(String refreshToken);
    public RootEntity<String> verifyEmail(String token);
    public RootEntity<String> forgotPassword(DtoForgotPasswordRequest request);
    public RootEntity<String> resetPassword(DtoResetPasswordRequest request);
}
