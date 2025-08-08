package com.dogukan.appointment.exception;

import lombok.Getter;

@Getter
public enum MessageType {
    USER_NOT_FOUND("1004","Kullanıcı bulunamadı."),
    EMAIL_NOT_VERIFIED("1005","E-posta adresi doğrulanmamış"),
    INVALID_TOKEN("1006","Geçersiz token."),
    APPOINTMENT_NOT_FOUND("1007","Verilen ID ile geçerli bir randevu bulunamadı."),
    TOKEN_EXPIRED("1008","Token süresi geçmiş."),
    APPOINTMENT_NOT_CANCELLABLE("1009","Randevu iptal edilemez."),
    DEPARTMENT_NOT_FOUND("1010","Bölüm bulunamadı."),
    DOCTOR_NOT_AVAILABLE("1011","Doktor müsait değil."),
    DOCTOR_NOT_FOUND("1012","Doktor bulunamadı."),
    EMAIL_ALREADY_EXISTS("1013","Bu e-posta adresi daha önce kullanılmış"),
    TCKNO_ALREADY_EXISTS("1014","Bu TC Kimlik numarası daha önce kullanılmış.");

    private String code;
    private String message;

    MessageType(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
