package com.dogukan.appointment.service.impl;

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
import com.dogukan.appointment.service.IAuthService;
import com.dogukan.appointment.service.IEmailService;
import com.dogukan.appointment.service.IRefreshTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final IRefreshTokenService refreshTokenService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final IEmailService emailService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                           AuthenticationManager authenticationManager, IRefreshTokenService refreshTokenService,
                           EmailVerificationTokenRepository emailVerificationTokenRepository, IEmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.emailService = emailService;
    }

    @Override
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

    @Override
    public DtoLoginResponse login(DtoLoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        Optional<User> user = userRepository.findByEmail(request.getEmail());
        if (user.isEmpty()) {
            throw new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND, "Bu e-posta ile kullanıcı bulunamadı."));
        }
        if (!user.get().getIsVerified()) {
            throw new BaseException(new ErrorMessage(MessageType.EMAIL_NOT_VERIFIED, "E-posta adresi doğrulanmamış."));
        }
        String accessToken = jwtService.generateToken(user.get());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.get());
        user.get().updateLastLogin();
        userRepository.save(user.get());
        return DtoLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getRefreshToken())
                .build();
    }

    @Override
    public RefreshTokenResponse refreshToken(String refreshToken) {
        String newAccessToken = refreshTokenService.generateNewAccessToken(refreshToken);
        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

    @Override
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

    @Override
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

    @Override
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