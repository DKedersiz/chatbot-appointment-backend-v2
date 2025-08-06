package com.dogukan.appointment.exception;

import lombok.Getter;

@Getter
public enum MessageType {
    USER_NOT_FOUND("1004","Kullanıcı bulunamadı.");

    private String code;
    private String message;

    MessageType(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
