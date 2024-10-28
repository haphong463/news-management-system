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
        String subject = "Đặt lại mật khẩu của bạn";

        // Định nghĩa các placeholder và giá trị tương ứng
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("Tên Người Nhận", event.getUsername());
        placeholders.put("Link Đặt Lại Mật khẩu", "link reset email");
        placeholders.put("Tên Công ty", "Công ty XYZ");
        placeholders.put("Địa chỉ Công ty", "123 Đường ABC, Thành phố XYZ");
        placeholders.put("Liên hệ", "contact@xyz.com");

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
