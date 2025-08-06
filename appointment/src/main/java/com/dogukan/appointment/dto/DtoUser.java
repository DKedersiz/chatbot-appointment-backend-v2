package com.dogukan.appointment.dto;

import com.dogukan.appointment.enums.Gender;
import com.dogukan.appointment.enums.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class DtoUser {
    private UUID userId;
    private String tckNo;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Gender gender;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isVerified;
    private Boolean isActive;
    private Role role;
    private LocalDateTime lastLogin;
}