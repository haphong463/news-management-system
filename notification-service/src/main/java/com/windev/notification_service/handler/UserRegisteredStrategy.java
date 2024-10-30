package com.windev.notification_service.handler;

import com.windev.notification_service.event.UserRegisteredEvent;
import com.windev.notification_service.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service("user-registered-strategy")
@Slf4j
public class UserRegisteredStrategy implements NotificationStrategy {

    @Autowired
    private EmailService emailService;

    @Override
    public void sendNotification(Object data) {
        if (!(data instanceof UserRegisteredEvent)) {
            log.error("Invalid data type for UserRegisteredStrategy");
            return;
        }

        UserRegisteredEvent event = (UserRegisteredEvent) data;
        log.info("--> Send a registration welcome email: {}", data);

        String to = event.getEmail();
        String subject = "Welcome to [[Company Name]]";

        // Define placeholders and their corresponding values
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("Company Name", "Your Company Name");
        placeholders.put("Confirmation Link", "confirmlink");
        placeholders.put("Company Address", "123 ABC Street, City XYZ");
        placeholders.put("Contact Information", "contact@yourcompany.com");
        placeholders.put("Recipient Name", event.getUsername());

        try {
            String htmlContent = emailService.getTemplateContent("email-templates/user_registered.html", placeholders);
            emailService.sendEmailHtml(to, subject, htmlContent);
            log.info("Registration email sent to {}", to);
        } catch (IOException e) {
            log.error("Error reading email template", e);
        } catch (MessagingException e) {
            log.error("Error sending email", e);
        }
    }
}
