package com.dogukan.appointment.dto.doctor;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DtoDoctorUpdate {

    private UUID departmentId;

    @Size(max = 100, message = "Ünvan 100 karakterden uzun olamaz.")
    private String title;

    @Size(max = 150, message = "Uzmanlık alanı 150 karakterden uzun olamaz.")
    private String specialization;

    @Min(value = 0, message = "Deneyim yılı 0'dan küçük olamaz.")
    @Max(value = 50, message = "Deneyim yılı 50'den büyük olamaz.")
    private Integer experienceYears;

    @Size(max = 500, message = "Hakkında bilgisi 500 karakterden uzun olamaz.")
    private String about;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Geçerli bir telefon numarası giriniz.")
    @Size(max = 15, message = "İş telefonu 15 karakterden uzun olamaz.")
    private String workPhone;

    @Email(message = "Geçerli bir iş e-posta adresi giriniz.")
    private String workEmail;

    @PastOrPresent(message = "İşe başlama tarihi bugün veya önceki bir tarih olmalıdır.")
    private LocalDateTime hireDate;

    private Boolean isAvailable;

    private Boolean isActive;


}
