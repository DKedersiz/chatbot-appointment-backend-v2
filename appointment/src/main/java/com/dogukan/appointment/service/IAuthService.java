package com.dogukan.appointment.service;

import com.dogukan.appointment.dto.*;
import com.dogukan.appointment.model.RootEntity;

public interface IAuthService {

    String register(DtoRegisterRequest request);
    DtoLoginResponse login(DtoLoginRequest request);
    RefreshTokenResponse refreshToken(String refreshToken);
    String verifyEmail(String token);
    String forgotPassword(DtoForgotPasswordRequest request);
    String resetPassword(DtoResetPasswordRequest request);
}
