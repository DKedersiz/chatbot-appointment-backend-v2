package com.dogukan.appointment.dto.user;

import com.dogukan.appointment.enums.Gender;
import com.dogukan.appointment.enums.Role;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DtoUserIU {
    @NotBlank(message = "TCK no boş olamaz")
    @Size(min = 11, max = 11, message = "TCK no 11 karakter olmalı")
    @Pattern(regexp = "^[1-9][0-9]{10}$", message = "Geçerli bir TC Kimlik numarası giriniz")
    private String tckNo;

    @NotBlank(message = "Ad boş olamaz")
    @Size(min = 3, max = 15, message = "Ad 3 ile 15 karakter arasında olmalı")
    private String firstName;

    @NotBlank(message = "Soyad boş olamaz")
    @Size(min = 2, max = 20, message = "Soyad 2 ile 20 karakter arasında olmalı")
    private String lastName;

    @NotBlank(message = "E-posta boş olamaz")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$", message = "Telefon numarası 10-15 haneli olmalı")
    private String phoneNumber;

    private Gender gender;

    @Size(max = 255, message = "Adres 255 karakterden uzun olamaz")
    private String address;

    @NotBlank(message = "Şifre boş olamaz", groups = CreateValidation.class)
    @Size(min = 6, message = "Şifre en az 6 karakter olmalı", groups = CreateValidation.class)
    private String password;

    @NotNull(message = "Rol boş olamaz", groups = CreateValidation.class)
    private Role role;

    public interface CreateValidation {}
}