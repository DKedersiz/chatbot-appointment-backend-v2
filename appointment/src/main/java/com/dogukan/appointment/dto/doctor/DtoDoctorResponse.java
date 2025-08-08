package com.dogukan.appointment.dto.doctor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DtoDoctorResponse {

    private UUID doctorId;
    private UUID userId;
    private UUID departmentId;
    private String title;
    private String specialization;
    private Integer experienceYears;
    private Boolean isAvailable;
    private Boolean isActive;
    private String about;
    private String workPhone;
    private String workEmail;
    private LocalDateTime hireDate;
    private LocalDateTime lastActivity;

}
