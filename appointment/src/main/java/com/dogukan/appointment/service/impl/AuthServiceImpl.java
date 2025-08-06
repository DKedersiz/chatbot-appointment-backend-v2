package com.dogukan.appointment.service.impl;

import com.dogukan.appointment.dto.*;
import com.dogukan.appointment.enums.Role;
import com.dogukan.appointment.jwt.JwtService;
import com.dogukan.appointment.model.EmailVerificationToken;
import com.dogukan.appointment.model.RefreshToken;
import com.dogukan.appointment.model.User;
import com.dogukan.appointment.repository.EmailVerificationTokenRepository;
import com.dogukan.appointment.repository.UserRepository;
import com.dogukan.appointment.service.IAuthService;
import com.dogukan.appointment.service.IEmailService;
import com.dogukan.appointment.service.IRefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    @Value("${JWT_SECRET_KEY}")
    private String secretKey;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final IRefreshTokenService refreshTokenService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final IEmailService emailService;



    @Override
    @Transactional
    public void register(DtoRegisterRequest request) {

        if(userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("E-posta hali hazırda kullanılıyor.");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .address(request.getAddress())
                .email(request.getEmail())
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

        emailService.sendVerificationEmail(savedUser.getEmail(),token);


    }

    @Override
    public DtoLoginResponse login(DtoLoginRequest request) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if(!user.getIsVerified()) {
            throw new EmailNotVerified("Email not verified");
        }

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        user.updateLastLogin();
        userRepository.save(user);

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
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        if(verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Verification token has expired.");
        }

        User user = verificationToken.getUser();
        user.setIsVerified(true);
        userRepository.save(user);

        emailVerificationTokenRepository.delete(verificationToken);

    }

    @Override
    public void forgotPassword(DtoForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email : " + request.getEmail()));

        String resetToken = UUID.randomUUID().toString();

    }

    @Override
    public void resetPassword(DtoResetPasswordRequest request) {

    }
}
