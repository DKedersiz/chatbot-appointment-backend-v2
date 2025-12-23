package com.dogukan.appointment.service.impl;

import com.dogukan.appointment.repository.EmailVerificationTokenRepository;
import com.dogukan.appointment.repository.UserRepository;
import com.dogukan.appointment.service.IEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    public EmailServiceImpl(JavaMailSender javaMailSender,TemplateEngine templateEngine,EmailVerificationTokenRepository emailVerificationTokenRepository,
                            UserRepository userRepository) {
        this.mailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.tokenRepository = emailVerificationTokenRepository;
        this.userRepository = userRepository;
    }

    @Value("${app_base_url}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;


    @Override
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

    @Override
    public void sendPasswordResetEmail(String email, String token   ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Şifre Sıfırlama");
            String resetLink = "http://localhost:8080/api/auth/reset-password?token=" + token;
            helper.setText("Şifrenizi sıfırlamak için aşağıdaki bağlantıya tıklayın: <a href=\"" + resetLink + "\">Şifreyi Sıfırla</a>", true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("E-posta gönderilemedi: " + e.getMessage());
        }
    }


}
