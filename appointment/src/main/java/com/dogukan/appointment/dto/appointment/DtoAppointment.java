package com.dogukan.appointment.dto.appointment;

import com.dogukan.appointment.enums.AppointmentStatus;
import com.dogukan.appointment.enums.CancelledBy;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class DtoAppointment {

    private UUID appointmentId;

    private UUID userId;
    private String patientName;
    private String patientEmail;
    private String patientPhone;

    private UUID doctorId;
    private String doctorName;
    private String doctorTitle;
    private String doctorSpecialization;

    private UUID departmentId;
    private String departmentName;

    private LocalDate appointmentDate;
    private LocalTime appointmentTime;

    private AppointmentStatus appointmentStatus;

    private String complaintText;
    private String notes;

    private String cancelReason;
    private LocalDateTime cancelledAt;
    private CancelledBy cancelledBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDate != null && appointmentTime != null ? appointmentDate.atTime(appointmentTime) : null;
    }

    public boolean isPastAppointment() {
        LocalDateTime appointmentDateTime = getAppointmentDateTime();
        return appointmentDateTime != null && appointmentDateTime.isBefore(LocalDateTime.now());
    }

    public boolean isToday() {
        return appointmentDate != null && appointmentDate.equals(LocalDateTime.now());
    }

    public boolean isTomorrow() {
        return appointmentDate != null && appointmentDate.equals(LocalDateTime.now().plusDays(1));
    }

    public boolean canBeCancelled() {
        return appointmentStatus == AppointmentStatus.PENDING || appointmentStatus == AppointmentStatus.CONFIRMED;
    }

    public boolean canBeRescheduled() {
        return canBeCancelled() && !isPastAppointment();
    }

    public String getStatusDisplay() {
        return appointmentStatus != null ? appointmentStatus.getDisplayName() : "Bilinmeyen";
    }

    public String getFormattedTime() {
        return appointmentTime != null ? appointmentTime.toString() : "";
    }

    public String getFormattedDate() {
        return appointmentDate != null ? appointmentDate.toString() : "";
    }

    public String getAppointmentInfo() {
        return String.format("%s - %s (%s)",getFormattedDate(),getFormattedTime(),doctorName);
    }




}
