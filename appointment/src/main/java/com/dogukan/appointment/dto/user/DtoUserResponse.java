
package com.dogukan.appointment.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class DtoUserResponse {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
}