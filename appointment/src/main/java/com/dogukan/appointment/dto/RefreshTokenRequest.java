package com.dogukan.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token boş olamaz.")
    private String refreshToken;
}
