package com.dogukan.appointment.enums;

public enum AppointmentStatus {
    PENDING("PENDING", "Beklemede"),
    CONFIRMED("CONFIRMED", "Onaylandı"),
    IN_PROGRESS("IN_PROGRESS", "Devam Ediyor"),
    COMPLETED("COMPLETED", "Tamamlandı"),
    CANCELLED("CANCELLED", "İptal Edildi"),
    NO_SHOW("NO_SHOW", "Gelmedi"),
    RESCHEDULED("RESCHEDULED", "Ertelendi");

    private final String code;
    private final String displayName;

    AppointmentStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return this == PENDING || this == CONFIRMED || this == IN_PROGRESS;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isCancelled() {
        return this == CANCELLED || this == NO_SHOW;
    }

    public boolean canBeModified() {
        return this == PENDING || this == CONFIRMED;
    }

    public boolean requiresAction() {
        return this == PENDING;
    }
}
