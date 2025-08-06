package com.dogukan.appointment.service;

public interface IEmailService {
    void sendVerificationEmail(String recipientEmail,String verificationToken);
    void sendPasswordResetEmail(String recipientEmail,String resetToken);
}
