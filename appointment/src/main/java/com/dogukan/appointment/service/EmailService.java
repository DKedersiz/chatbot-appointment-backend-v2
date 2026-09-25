package com.dogukan.appointment.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        this.mailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    @Value("${app_base_url}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;


    public void sendVerificationEmail(String recipientEmail,String verificationToken) {
        String verificationUrl = baseUrl + "/email-dogrulama?token=" + verificationToken;
        String subject = "Hesabınızı Doğrulayın";

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,"utf-8");

            Context context = new Context();
            context.setVariable("verificationUrl",verificationUrl);

            String htmlContent = templateEngine.process("email-verification",context);

            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent,true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Email gönderilemedi: " + recipientEmail,e);
        }
    }

    public void sendPasswordResetEmail(String email, String token   ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Şifre Sıfırlama");
            String resetLink = baseUrl + "/sifre-sifirla?token=" + token;
            helper.setText("Şifrenizi sıfırlamak için aşağıdaki bağlantıya tıklayın: <a href=\"" + resetLink + "\">Şifreyi Sıfırla</a>", true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("E-posta gönderilemedi: " + e.getMessage());
        }
    }


}
