package com.dogukan.appointment.enums;

public enum ChatSessionStatus {
    ACTIVE("ACTIVE", "Aktif"),
    COMPLETED("COMPLETED", "Tamamlandı"),
    ABANDONED("ABANDONED", "Terk Edildi"),
    EXPIRED("EXPIRED", "Süresi Doldu"),
    ERROR("ERROR", "Hata");

    private final String code;
    private final String displayName;

    ChatSessionStatus(String code, String displayName) {
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
        return this == ACTIVE;
    }

    public boolean isFinished() {
        return this == COMPLETED || this == ABANDONED || this == EXPIRED || this == ERROR;
    }

    public boolean isSuccessful() {
        return this == COMPLETED;
    }
}