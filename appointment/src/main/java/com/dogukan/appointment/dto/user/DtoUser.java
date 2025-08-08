package com.dogukan.appointment.dto.user;

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
    public UUID userId;
    public String tckNo;
    public String firstName;
    public String lastName;
    public String email;
    public String phoneNumber;
    public Gender gender;
    public String address;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public Boolean isVerified;
    public Boolean isActive;
    public Role role;
    public LocalDateTime lastLogin;
}