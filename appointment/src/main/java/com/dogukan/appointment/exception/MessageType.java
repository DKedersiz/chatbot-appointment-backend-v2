package com.dogukan.appointment.exception;

import lombok.Getter;

@Getter
public enum MessageType {
    USER_NOT_FOUND("1004","Kullanıcı bulunamadı."),
    EMAIL_NOT_VERIFIED("1005","E-posta adresi doğrulanmamış"),
    INVALID_TOKEN("1006","Geçersiz token."),
    TOKEN_EXPIRED("1007","Token süresi geçmiş.");

    private String code;
    private String message;

    MessageType(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
