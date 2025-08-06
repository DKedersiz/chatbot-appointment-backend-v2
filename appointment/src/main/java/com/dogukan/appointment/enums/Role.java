package com.dogukan.appointment.enums;

public enum Role {
    USER("HASTA"),
    ADMIN("Yönetici"),
    DOCTOR("Doktor");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean hasAuthority(String authority) {
        return ("ROLE_" + this.name()).equals(authority);
    }
}
