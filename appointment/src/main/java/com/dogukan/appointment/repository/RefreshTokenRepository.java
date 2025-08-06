package com.dogukan.appointment.repository;

import com.dogukan.appointment.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByRefreshToken(String refreshToken);

}
