package com.dogukan.appointment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Setter
public class DtoForgotPasswordRequest {

    @NotBlank(message = "E-posta adresi boş bırakılamaz.")
    @Email(message= "Geçerli bir e-posta adresi giriniz.")
    private String email;
}
