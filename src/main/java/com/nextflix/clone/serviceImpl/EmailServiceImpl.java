package com.nextflix.clone.serviceImpl;

import com.nextflix.clone.exception.EmailNotVerifiedException;
import com.nextflix.clone.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:4200 }")
    private String frontendUrl;

    @Value("${spring.mail.username")
    private String fromEmail;

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Nextflix Clone - Email Verification");

            String verificationLink = frontendUrl + "/verify-email?token=" + token;

            String emialBody = "Dear User,\n\n" +
                    "Thank you for registering with Nextflix Clone! Please click the link below to verify your email address:\n\n" +
                    verificationLink + "\n\n" +
                    "If you did not create an account, please ignore this email.\n\n" +
                    "Best regards,\n" +
                    "Nextflix Clone Team";

            message.setText(emialBody);
            mailSender.send(message);
        } catch (Exception ex ) {
            logger.error("Failed to send verification email to {}: {}", toEmail , ex.getMessage());
            throw new EmailNotVerifiedException("Failed to send verification email. Please try again later.");
        }
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Nextflix Clone - Password Reset");

            String resetLink = frontendUrl + "/reset-password?token=" + token;
            String emailBody = "Dear User,\n\n" +
                    "We received a request to reset your password for your Nextflix Clone account. Please click the link below to reset your password:\n\n" +
                    resetLink + "\n\n" +
                    "If you did not request a password reset, please ignore this email.\n\n" +
                    "Best regards,\n" +
                    "Nextflix Clone Team";

            message.setText(emailBody);
            mailSender.send(message);

            logger.info("Password reset email sent to {}", toEmail);
        }
        catch (Exception ex) {
            logger.error("Failed to send password reset email to {}" , ex.getMessage());
//
        }
    }
}
