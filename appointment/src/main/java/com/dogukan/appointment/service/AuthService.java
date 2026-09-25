package com.dogukan.appointment.service;

import com.dogukan.appointment.dto.*;
import com.dogukan.appointment.enums.Role;
import com.dogukan.appointment.exception.BaseException;
import com.dogukan.appointment.exception.ErrorMessage;
import com.dogukan.appointment.exception.MessageType;
import com.dogukan.appointment.jwt.JwtService;
import com.dogukan.appointment.model.EmailVerificationToken;
import com.dogukan.appointment.model.RefreshToken;
import com.dogukan.appointment.model.User;
import com.dogukan.appointment.repository.EmailVerificationTokenRepository;
import com.dogukan.appointment.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                           AuthenticationManager authenticationManager, RefreshTokenService refreshTokenService,
                           EmailVerificationTokenRepository emailVerificationTokenRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.emailService = emailService;
    }

    @Transactional
    public String register(DtoRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BaseException(new ErrorMessage(MessageType.EMAIL_ALREADY_EXISTS, "E-posta hali hazırda kullanılıyor."));
        }
        if (userRepository.existsByTckNo(request.getTckNo())) {
            throw new BaseException(new ErrorMessage(MessageType.TCKNO_ALREADY_EXISTS, "TC Kimlik numarası hali hazırda kullanılıyor."));
        }
        User user = User.builder()
                .tckNo(request.getTckNo())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .address(request.getAddress())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .gender(request.getGender())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .isVerified(false)
                .isActive(true)
                .build();
        User savedUser = userRepository.save(user);
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .user(savedUser)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
        emailVerificationTokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(savedUser.getEmail(), token);
        return "Kayıt başarıyla oluşturuldu. Lütfen e-postanızı kontrol edip hesabınızı onaylayın.";
    }

    public DtoLoginResponse login(DtoLoginRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException e) {
            userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
                user.incrementFailedLoginAttempts();
                userRepository.save(user);
            });
            throw e;
        }

        User authenticatedUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND, request.getEmail())));

        String accessToken = jwtService.generateToken(authenticatedUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(authenticatedUser);

        authenticatedUser.resetFailedLoginAttempts();
        authenticatedUser.updateLastLogin();
        userRepository.save(authenticatedUser);

        return DtoLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getRefreshToken())
                .userId(authenticatedUser.getUserId())
                .firstName(authenticatedUser.getFirstName())
                .lastName(authenticatedUser.getLastName())
                .email(authenticatedUser.getEmail())
                .role(authenticatedUser.getRole().name())
                .build();
    }

    public String refreshToken(String refreshToken) {
        return refreshTokenService.generateNewAccessToken(refreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }

    @Transactional
    public String verifyEmail(String token) {
        Optional<EmailVerificationToken> verificationToken = emailVerificationTokenRepository.findByToken(token);
        if (verificationToken.isEmpty()) {
            throw new BaseException(new ErrorMessage(MessageType.INVALID_TOKEN, "Geçersiz token."));
        }
        if (verificationToken.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BaseException(new ErrorMessage(MessageType.TOKEN_EXPIRED, "Verification token has expired."));
        }
        User user = verificationToken.get().getUser();
        user.setIsVerified(true);
        userRepository.save(user);
        emailVerificationTokenRepository.delete(verificationToken.get());
        return "E-posta başarıyla doğrulandı.";
    }

    @Transactional
    public String forgotPassword(DtoForgotPasswordRequest request) {
        Optional<User> user = userRepository.findByEmail(request.getEmail());
        if (user.isEmpty()) {
            throw new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND, "Bu e-posta ile kullanıcı bulunamadı."));
        }
        String resetToken = UUID.randomUUID().toString();
        EmailVerificationToken resetPasswordToken = EmailVerificationToken.builder()
                .token(resetToken)
                .user(user.get())
                .expiryDate(LocalDateTime.now().plusHours(1))
                .build();
        emailVerificationTokenRepository.save(resetPasswordToken);
        emailService.sendPasswordResetEmail(user.get().getEmail(), resetToken);
        return "Şifre sıfırlama isteği gönderildi.";
    }

    @Transactional
    public String resetPassword(DtoResetPasswordRequest request) {
        Optional<EmailVerificationToken> resetToken = emailVerificationTokenRepository.findByToken(request.getToken());
        if (resetToken.isEmpty()) {
            throw new BaseException(new ErrorMessage(MessageType.INVALID_TOKEN, "Geçersiz şifre sıfırlama token’ı."));
        }
        if (resetToken.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BaseException(new ErrorMessage(MessageType.TOKEN_EXPIRED, "Şifre sıfırlama token’ı süresi dolmuş."));
        }
        User user = resetToken.get().getUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        emailVerificationTokenRepository.delete(resetToken.get());
        return "Şifre sıfırlama isteği gönderildi.";
    }
}