package com.dogukan.appointment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DtoResetPasswordRequest {

    @NotBlank(message = "Token boş bırakılamaz.")
    private String token;

    @NotBlank(message = "Yeni şifre alanı boş bırakılamaz.")
    @Size(min = 8, message = "Yeni şifre en az 8 karakter içermelidir.")
    private String password;
}
