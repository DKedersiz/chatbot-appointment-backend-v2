package com.dogukan.appointment.enums;

public enum AppointmentStatus {
    PENDING("Beklemede"),
    CONFIRMED("Onaylandı"),
    IN_PROGRESS("Devam Ediyor"),
    COMPLETED("Tamamlandı"),
    CANCELLED("İptal Edildi"),
    NO_SHOW("Gelmedi"),
    RESCHEDULED("Ertelendi");

    private final String displayName;

    AppointmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
