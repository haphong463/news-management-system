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
        String subject = "Chào mừng đến với Công ty XYZ";

        // Định nghĩa các placeholder và giá trị tương ứng
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("Tên Người Nhận", event.getUsername());
        placeholders.put("Link Xác Nhận", "http://localhost:8081/api/v1/confirm");
        placeholders.put("Tên Công ty", "Công ty XYZ");
        placeholders.put("Địa chỉ Công ty", "123 Đường ABC, Thành phố XYZ");
        placeholders.put("Liên hệ", "contact@xyz.com");

        try {
            String htmlContent = emailService.getTemplateContent("templates/user_registered.html", placeholders);
            emailService.sendEmailHtml(to, subject, htmlContent);
            log.info("Registration email sent to {}", to);
        } catch (IOException e) {
            log.error("Error reading email template", e);
        } catch (MessagingException e) {
            log.error("Error sending email", e);
        }
    }
}
