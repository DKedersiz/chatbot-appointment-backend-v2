package com.dogukan.appointment.controller;

import com.dogukan.appointment.dto.*;
import com.dogukan.appointment.dto.user.DtoUserResponse;
import com.dogukan.appointment.exception.BaseException;
import com.dogukan.appointment.model.RootEntity;
import com.dogukan.appointment.security.UserPrincipal;
import com.dogukan.appointment.service.AuthService;
import com.dogukan.appointment.exception.ErrorMessage;
import com.dogukan.appointment.exception.MessageType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true", exposedHeaders = "Set-Cookie")
@RequestMapping(path = "/rest/api/auth")
public class AuthController {

    private static final String REFRESH_TOKEN_PATH = "/rest/api/auth/refreshToken";
    private static final boolean IS_LOCALHOST = true;

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    private static String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    @PostMapping(path = "/register")
    public RootEntity<String> register(@Valid @RequestBody DtoRegisterRequest request) {
        return RootEntity.ok(authService.register(request));
    }

    @PostMapping(path = "/login")
    public RootEntity<DtoUserResponse> login(
            @Valid @RequestBody DtoLoginRequest request,
            HttpServletResponse response) {

        DtoLoginResponse dto = authService.login(request);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", dto.getAccessToken())
                .httpOnly(true)
                .secure(!IS_LOCALHOST)
                .path("/")
                .maxAge(60 * 15)
                .sameSite("Strict")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", dto.getRefreshToken())
                .httpOnly(true)
                .secure(!IS_LOCALHOST)
                .path(REFRESH_TOKEN_PATH)
                .maxAge(60 * 60 * 24 * 7)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        DtoUserResponse userResponse = DtoUserResponse.builder()
                .userId(dto.getUserId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .role(dto.getRole())
                .build();

        return RootEntity.ok(userResponse);
    }

    @PostMapping(path = "/refreshToken")
    public RootEntity<String> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = readCookie(request, "refreshToken");
        if (refreshToken == null) {
            throw new BaseException(new ErrorMessage(MessageType.INVALID_TOKEN, "Refresh token bulunamadı."));
        }

        String newAccessToken = authService.refreshToken(refreshToken);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .secure(!IS_LOCALHOST)
                .path("/")
                .maxAge(60 * 15)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        return RootEntity.ok("Token yenilendi.");
    }

    @GetMapping(path = "/verifyEmail")
    public RootEntity<String> verifyEmail(@RequestParam String token) {
        return RootEntity.ok(authService.verifyEmail(token));
    }

    @PostMapping(path = "/forgotPassword")
    public RootEntity<String> forgotPassword(@Valid @RequestBody DtoForgotPasswordRequest request) {
        return RootEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping(path = "/resetPassword")
    public RootEntity<String> resetPassword(@Valid @RequestBody DtoResetPasswordRequest request) {
        return RootEntity.ok(authService.resetPassword(request));
    }

    @PostMapping(path = "/logout")
    public RootEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = readCookie(request, "refreshToken");
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(!IS_LOCALHOST)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(!IS_LOCALHOST)
                .path(REFRESH_TOKEN_PATH)
                .maxAge(0)
                .sameSite("Strict")
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
        if (!(principal instanceof UserPrincipal)) {
            return ResponseEntity.status(401).body(null);
        }

        UserPrincipal user = (UserPrincipal) principal;

        DtoUserResponse response = DtoUserResponse.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getUsername())
                .role(user.getRole().name())
                .build();

        return ResponseEntity.ok(RootEntity.ok(response));
    }
}