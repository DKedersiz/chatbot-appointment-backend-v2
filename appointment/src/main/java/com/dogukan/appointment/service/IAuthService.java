package com.dogukan.appointment.service;

import com.dogukan.appointment.dto.*;

public interface IAuthService {

    void register(DtoRegisterRequest request);
    DtoLoginResponse login(DtoLoginRequest request);
    RefreshTokenResponse refreshToken(String refreshToken);
    void verifyEmail(String token);
    void forgotPassword(DtoForgotPasswordRequest request);
    void resetPassword(DtoResetPasswordRequest request);
}
