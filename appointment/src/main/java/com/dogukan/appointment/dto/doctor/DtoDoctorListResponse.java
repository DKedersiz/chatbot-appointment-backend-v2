// src/main/java/com/dogukan/appointment/dto/doctor/DtoDoctorListResponse.java
package com.dogukan.appointment.dto.doctor;

import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DtoDoctorListResponse {
    private UUID doctorId;
    private String name;
    private String surname;
    private String fullName;
    private String departmentName;
    private String title;
    private String specialization;
    private Integer experienceYears;
    private Boolean isAvailable;
    private Boolean isActive;
    private String about;
    private String imageUrl;
}