package com.dogukan.appointment.dto.user;

import com.dogukan.appointment.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DtoUserUpdate {

    @Size(min = 1, max = 50)
    @NotBlank
    private String firstName;
    @Size(min = 1, max = 50)
    @NotBlank
    private String lastName;

    private String phoneNumber;
    @Size(min = 1, max = 100,message = "Adres alanı 100 karakterden fazla olamaz.")
    private String address;

    private Gender gender;
}
