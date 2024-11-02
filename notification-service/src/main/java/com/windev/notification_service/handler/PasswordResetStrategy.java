package com.windev.notification_service.handler;

import com.windev.notification_service.event.PasswordResetEvent;
import com.windev.notification_service.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service("password-reset-strategy")
@Slf4j
public class PasswordResetStrategy implements NotificationStrategy {

    @Autowired
    private EmailService emailService;

    @Override
    public void sendNotification(Object data) {
        if (!(data instanceof PasswordResetEvent)) {
            log.error("Invalid data type for PasswordResetStrategy");
            return;
        }

        PasswordResetEvent event = (PasswordResetEvent) data;
        log.info("--> Send a password reset email: {}", data);

        String to = event.getEmail();
        String subject = "Reset Your Password";

        // Define placeholders and their corresponding values
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("Company Name", "Your Company Name");
        placeholders.put("Password Reset Link", "passwordresetlink");
        placeholders.put("Company Address", "123 ABC Street, City XYZ");
        placeholders.put("Contact Information", "contact@yourcompany.com");
        placeholders.put("Recipient Name", event.getUsername());

        try {
            String htmlContent = emailService.getTemplateContent("templates/password_reset.html", placeholders);
            emailService.sendEmailHtml(to, subject, htmlContent);
            log.info("Password reset email sent to {}", to);
        } catch (IOException e) {
            log.error("Error reading email template", e);
        } catch (MessagingException e) {
            log.error("Error sending email", e);
        }
    }
}
