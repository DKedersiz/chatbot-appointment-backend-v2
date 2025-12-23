package com.dogukan.appointment.enums;

public enum Gender {
    ERKEK("Erkek"),
    KADIN("Kadın");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
