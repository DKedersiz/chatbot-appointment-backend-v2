package com.dogukan.appointment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "doctor_schedules",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"doctor_id", "day_of_week", "start_time"})
        },
        indexes = {
                @Index(name = "idx_schedule_doctor", columnList = "doctor_id"),
                @Index(name = "idx_schedule_day", columnList = "day_of_week"),
                @Index(name = "idx_schedule_active", columnList = "is_active")
        })
public class DoctorSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "schedule_id", updatable = false, nullable = false)
    private UUID scheduleId;

    @NotNull(message = "Doktor bilgisi gereklidir.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id",nullable = false)
    private Doctor doctor;

    @NotNull(message = "Haftanın günü gereklidir.")
    @Min(value = 1, message = "Haftanın günü 1-7 arası olmalıdır. (1=Pazartesi)")
    @Max(value = 7,message =  "Haftanın günü 1-7 arası olmalıdır. (7=Pazar")
    @Column(name = "day_of_week",nullable = false)
    private Integer dayOfWeek;

    @NotNull(message = "Başlangıç saati gereklidir.")
    @Column(name = "start_time",nullable = false)
    private LocalTime startTime;

    @NotNull(message = "Bitiş saati gereklidir.")
    @Column(name = "end_time",nullable = false)
    private LocalTime endTime;

    @Column(name = "lunch_break_start")
    private LocalTime lunchBreakStart;

    @Column(name = "lunch_break_end")
    private LocalTime lunchBreakEnd;

    @Min(value = 5, message = "Randevu süresi en az 5 dakika olmalıdır.")
    @Max(value = 480, message = "Randevu süresi en fazla 8 saat olabilir.")
    @Builder.Default
    @Column(name = "appointment_duration", nullable = false)
    private Integer appointmentDuration = 30;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

}
