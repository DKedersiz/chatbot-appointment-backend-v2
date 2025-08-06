package com.dogukan.appointment.dto;

import com.dogukan.appointment.enums.Gender;
import com.dogukan.appointment.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DtoUserIU {

    @NotBlank(message = "TCK no cannot be blank")
    @Size(min = 11, max = 11, message = "TCK no must be 11 characters")
    private String tckNo;

    @NotBlank(message = "First name cannot be blank")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be between 10-15 digits")
    private String phoneNumber;

    private Gender gender;

    private String address;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Role cannot be null")
    private Role role;
}