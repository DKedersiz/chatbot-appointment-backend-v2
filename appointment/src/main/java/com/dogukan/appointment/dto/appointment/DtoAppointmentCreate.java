package com.dogukan.appointment.dto.appointment;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class DtoAppointmentCreate {

    @NotNull(message = "Hasta bilgisi gereklidir")
    private UUID userId;

    @NotNull(message = "Doktor bilgisi gereklidir.")
    private UUID doctorId;

    @NotNull(message = "Bölüm bilgisi gereklidir.")
    private UUID departmentId;

    @NotNull(message = "Randevu tarihi gereklidir")
    @FutureOrPresent(message = "Randevu tarihi bugün ya da gelecek bir tarih olmalıdır.")
    private LocalDate appointmentDate;

    @NotNull(message = "Randevu tarihi gereklidir")
    private LocalTime appointmentTime;

    @Size(max = 1000,message = "Şikayet metni 1000 karakteri geçmemelidir.")
    private String complaintText;

    @Size(max = 1000,message = "Notlar 1000 karakteri geçmemelidir.")
    private String notes;

    @AssertTrue(message = "Randevu saati çalışma saatleri içinde olmalıdır.")
    public boolean isValidAppointmentTime() {
        if(appointmentTime == null)
            return true;

        LocalTime startTime = LocalTime.of(9,0);
        LocalTime endTime = LocalTime.of(17,0);

        return !appointmentTime.isBefore(startTime) && !appointmentTime.isAfter(endTime);
    }

    @AssertTrue(message = "Hafta sonuna randevu alınamaz.")
    public boolean isValidAppointmentDate() {
        if(appointmentDate == null)
            return true;

        int dayOfWeek = appointmentDate.getDayOfWeek().getValue();
        return dayOfWeek >= 1 && dayOfWeek <= 5;
    }

}
