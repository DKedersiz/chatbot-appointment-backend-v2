package com.dogukan.appointment.model;

import com.dogukan.appointment.enums.AppointmentStatus;
import com.dogukan.appointment.enums.CancelledBy;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appointment",indexes = {
        @Index(name = "idx_appointment_user",columnList = "user_id"),
        @Index(name = "idx_appointment_doctor",columnList = "doctor_id"),
        @Index(name = "idx_appointment_date",columnList = "appointment_date"),
        @Index(name = "idx_appointment_status",columnList = "status"),
        @Index(name = "idx_appointment_user_date",columnList = "user_id, appointment_date"),
        @Index(name = "idx_appointment_doctor_date",columnList = "doctor_id, appointment_date")
})
public class    Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "appointment_id",updatable = false,nullable = false)
    private UUID appointmentId;


    @NotNull(message = "Hasta bilgisi gereklidir.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @NotNull(message = "Doktor bilgisi gereklidir.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id",nullable = false)
    private Doctor doctor;

    @NotNull(message = "Randevu tarihi gereklidir.")
    @Column(name = "appointment_date",nullable = false)
    private LocalDate appointmentDate;

    @NotNull(message = "Randevu saati gereklidir.")
    @Column(name = "appointment_time",nullable = false)
    private LocalTime appointmentTime;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Size(max = 1000,message = "Şikayet alanı 1000 karakterden uzun olamaz.")
    @Column(name = "complaint_text",columnDefinition = "TEXT")
    private String complaintText;

    @Size(max = 1000,message = "Notlar 1000 karakterden uzun olamaz.")
    @Column(name = "notes",columnDefinition = "TEXT")
    private String notes;

    @Size(max = 500,message = "İptal nedeni 500 karakterden uzun olamaz.")
    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancelled_by")
    private CancelledBy cancelledBy;

    @CreationTimestamp
    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    // department, doktorun bağlı olduğu bölümden türetilir; ayrıca saklanmaz ki ikisi asla uyuşmazlığa düşmesin.
    public Department getDepartment() {
        return doctor != null ? doctor.getDepartment() : null;
    }

    public boolean canBeCancelled() {
        if(status != AppointmentStatus.PENDING && status != AppointmentStatus.CONFIRMED) {
            return false;
        }

        LocalDateTime appointmentDateTime = LocalDateTime.of(appointmentDate, appointmentTime);
        if(appointmentDateTime.isBefore(LocalDateTime.now())) {
            return false;
        }

        LocalDateTime cancellationDeadline = appointmentDateTime.minusHours(24);
        return LocalDateTime.now().isBefore(cancellationDeadline);
    }


}
