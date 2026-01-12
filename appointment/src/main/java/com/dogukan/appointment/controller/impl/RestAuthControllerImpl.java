package com.dogukan.appointment.controller.impl;

import com.dogukan.appointment.controller.IRestAuthController;
import com.dogukan.appointment.controller.RestBaseController;
import com.dogukan.appointment.dto.*;
import com.dogukan.appointment.dto.user.DtoUserResponse;
import com.dogukan.appointment.model.RootEntity;
import com.dogukan.appointment.model.User;
import com.dogukan.appointment.service.IAuthService;
import com.dogukan.appointment.exception.ErrorMessage;
import com.dogukan.appointment.exception.MessageType;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true", exposedHeaders = "Set-Cookie")
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
    public RootEntity<DtoLoginResponse> login(
            @Valid @RequestBody DtoLoginRequest request,
            HttpServletResponse response) {

        DtoLoginResponse dto = authService.login(request);

        boolean isLocalhost = true;

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", dto.getAccessToken())
                .httpOnly(true)
                .secure(!isLocalhost)
                .path("/")
                .maxAge(60 * 15)
                .sameSite("Strict")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", dto.getRefreshToken())
                .httpOnly(true)
                .secure(!isLocalhost)
                .path("/api/auth/refresh-token")
                .maxAge(60 * 60 * 24 * 7)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        return RootEntity.ok(dto);
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

    @PostMapping(path = "/logout")
    public RootEntity<String> logout(HttpServletResponse response) {
        boolean isLocalhost = true;

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(!isLocalhost)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(!isLocalhost)
                .path("/api/auth/refresh-token")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        return RootEntity.ok("Çıkış başarılı.");
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RootEntity<DtoUserResponse>> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body(null);
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return ResponseEntity.status(401).body(null);
        }

        User user = (User) principal;

        DtoUserResponse response = DtoUserResponse.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();

        return ResponseEntity.ok(RootEntity.ok(response));
    }
}