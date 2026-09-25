package com.dogukan.appointment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "doctors", indexes = {
        @Index(name = "idx_doctor_user",columnList = "user_id"),
        @Index(name = "idx_doctor_department",columnList = "department_id"),
        @Index(name = "idx_doctor_specialization",columnList = "specialization"),
        @Index(name = "idx_doctor_available",columnList = "is_available")
})
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "doctor_id", updatable = false, nullable = false)
    private UUID doctorId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false,unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id",nullable = false)
    private Department department;

    @NotBlank(message = "Ünvan boş olamaz")
    @Size(max = 100,message = "Ünvan 100 karakterden uzun olamaz.")
    @Column(name = "title",nullable = false,length = 100)
    private String title;

    @NotBlank(message = "Uzmanlık alanı boş olamaz.")
    @Size(max = 150,message = "Uzmanlık alanı 150 karakterden uzun olamaz.")
    @Column(name = "specialization",nullable = false,length = 150)
    private String specialization;

    @NotNull(message = "Deneyim yılı boş olamaz")
    @Min(value = 0,message = "Deneyim yılı 0'dan küçük olamaz.")
    @Max(value = 50,message = "Deneyim yılı 50'den büyük olamaz.")
    @Column(name = "experience_years",nullable = false)
    private Integer experienceYears;

    @Builder.Default
    @Column(name = "is_available",nullable = false)
    private Boolean isAvailable = true;

    @Builder.Default
    @Column(name = "is_active",nullable = false)
    private Boolean isActive = true;

    @Size(max = 500,message = "Hakkında bilgisi 500 karakterden uzun olamaz.")
    @Column(name = "about",length = 500)
    private String about;

    @Column(name = "work_phone",length = 15)
    private String workPhone;

    @Email(message = "Geçerli bir iş e-posta adresi giriniz.")
    @Column(name = "work_email")
    private String workEmail;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "hire_date")
    private LocalDateTime hireDate;

    public String getDisplayName() {
        return user != null ? title + " " + user.getFullName() : null;
    }

    public boolean isActiveAndAvailable() {
        return isActive && isAvailable && user != null && user.getIsActive();
    }

}
