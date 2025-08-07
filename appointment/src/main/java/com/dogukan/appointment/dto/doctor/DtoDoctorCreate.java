package com.dogukan.appointment.dto.doctor;

import com.dogukan.appointment.model.User;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DtoDoctorCreate {

    @NotNull(message = "Kullanıcı bilgisi gereklidir.")
    private UUID userId;

    @NotNull(message = "Bölüm bilgisi gereklidir.")
    private UUID departmentId;

    @NotBlank(message = "Ünvan boş olamaz.")
    @Size(max = 100,message = "Ünvan 100 karakterden fazla olamaz.")
    private String title;

    @NotBlank(message = "Uzmanlık alanı boş olamaz.")
    @Size(max = 100,message = "Uzmanlık alanı 100 karakterden fazla olamaz.")
    private String specialization;

    @NotNull(message = "Deneyim yılı boş olamaz.")
    @Min(value = 0, message = "Deneyim yılı 0'dan küçük olamaz.")
    @Max(value = 50, message = "Deneyim yılı 50'den büyük olamaz.")
    private Integer experienceYears;

    @Size(max = 500,message = "Hakkında bilgisi 500 karakterden fazla olamaz.")
    private String about;

    private String workPhone;

    @Email(message = "Geçerli bir iş e-posta adresi giriniz.")
    private String workEmail;

    @PastOrPresent(message = "İşe başlama tarihi bugün veya önceki bir tarih olmalıdır.")
    private LocalDateTime hireDate;

    @Builder.Default
    private Boolean isAvailable = true;

    @Builder.Default
    private Boolean isActive = true;


}
