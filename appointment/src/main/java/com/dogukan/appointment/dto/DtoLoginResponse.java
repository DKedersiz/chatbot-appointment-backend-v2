package com.dogukan.appointment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DtoLoginResponse {

    private String accessToken;
    private String refreshToken;
}
