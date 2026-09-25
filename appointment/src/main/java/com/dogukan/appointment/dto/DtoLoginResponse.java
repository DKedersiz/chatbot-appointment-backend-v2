package com.dogukan.appointment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class DtoLoginResponse {

    private String accessToken;
    private String refreshToken;

    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
}