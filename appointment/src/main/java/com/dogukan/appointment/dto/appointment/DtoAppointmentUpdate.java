package com.dogukan.appointment.dto.appointment;

import com.dogukan.appointment.enums.AppointmentStatus;
import com.dogukan.appointment.enums.CancelledBy;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DtoAppointmentUpdate {

    public UUID doctorId;

    private UUID departmentId;

    @FutureOrPresent(message = "Randevu tarihi bugün ya da gelecek bir tarih olmalıdır.")
    private LocalDate appointmentDate;

    private LocalTime appointmentTime;

    private AppointmentStatus appointmentStatus;

    @Size(max = 1000,message = "Şikayet metni 1000 karakterden uzun olamaz.")
    private String complaintText;

    @Size(max = 1000,message = "Notlar 1000 karakterden uzun olamaz.")
    private String notes;

    @Size(max = 500,message = "İptal nedeni 500 karakterden fazla olamaz.")
    private String cancelReason;

    private CancelledBy cancelledBy;

    public boolean isRescheduling() {
        return appointmentDate != null || appointmentTime != null || doctorId != null;
    }

    public boolean isCancelling() {
        return appointmentStatus == AppointmentStatus.CANCELLED || cancelReason != null || cancelledBy != null;
    }

    @AssertTrue(message = "Randevu saati çalışma saatleri içinde olmalıdır (09:00 - 17:00).")
    public boolean isValidAppointmentTime() {
        if (appointmentTime == null) return true;

        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);

        return !appointmentTime.isBefore(startTime) && !appointmentTime.isAfter(endTime);
    }

    @AssertTrue(message = "Hafta sonu randevu alınamaz.")
    public boolean isValidAppointmentDate() {
        if (appointmentDate == null) return true;

        int dayOfWeek = appointmentDate.getDayOfWeek().getValue();
        return dayOfWeek >= 1 && dayOfWeek <= 5;
    }

    @AssertTrue(message = "İptal işlemi için iptal nedeni gereklidir.")
    public boolean isValidCancellation() {
        if (appointmentStatus == AppointmentStatus.CANCELLED) {
            return cancelReason != null && !cancelReason.trim().isEmpty() && cancelledBy != null;
        }
        return true;
    }
}
