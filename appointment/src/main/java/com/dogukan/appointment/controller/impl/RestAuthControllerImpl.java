package com.dogukan.appointment.controller.impl;

import com.dogukan.appointment.controller.IRestAuthController;
import com.dogukan.appointment.controller.RestBaseController;
import com.dogukan.appointment.dto.*;
import com.dogukan.appointment.model.RootEntity;
import com.dogukan.appointment.service.IAuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/rest/api/auth")
public class RestAuthControllerImpl extends RestBaseController implements IRestAuthController  {

    private final IAuthService authService;

    public RestAuthControllerImpl(IAuthService authService) {
        this.authService = authService;
    }

    @Override
    @PostMapping(path = "/register")
    public RootEntity<String> register(@Valid @RequestBody DtoRegisterRequest request) {
        return RootEntity.ok(authService.register(request));
    }

    @Override
    @PostMapping(path = "/login")
    public RootEntity<DtoLoginResponse> login(@Valid @RequestBody DtoLoginRequest request) {
        return RootEntity.ok(authService.login(request));
    }

    @Override
    @PostMapping(path = "/refreshToken")
    public RootEntity<RefreshTokenResponse> refreshToken(@RequestBody String refreshToken) {
        return RootEntity.ok(authService.refreshToken(refreshToken));
    }

    @Override
    @GetMapping(path = "/verifyEmail")
    public RootEntity<String> verifyEmail(@RequestParam String token) {
        return RootEntity.ok(authService.verifyEmail(token));
    }

    @Override
    @PostMapping(path = "/forgotPassword")
    public RootEntity<String> forgotPassword(@Valid @RequestBody DtoForgotPasswordRequest request) {
        return RootEntity.ok(authService.forgotPassword(request));
    }

    @Override
    @PostMapping(path = "/resetPassword")
    public RootEntity<String> resetPassword(@Valid @RequestBody DtoResetPasswordRequest request) {
        return RootEntity.ok(authService.resetPassword(request));
    }
}
