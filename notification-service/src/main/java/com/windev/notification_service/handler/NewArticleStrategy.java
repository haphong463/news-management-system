package com.windev.notification_service.handler;

import com.windev.notification_service.event.NewArticleEvent;
import com.windev.notification_service.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service("new-article-strategy")
@Slf4j
@RequiredArgsConstructor
public class NewArticleStrategy implements NotificationStrategy {
    private final EmailService emailService;

    @Override
    public void sendNotification(Object data) {
        NewArticleEvent event = (NewArticleEvent) data;

        log.info("--> Send an announcement for new article: {}", data);

        String to = "haphong463@gmail.com";
        String subject = "Bài viết mới từ Công ty XYZ";

        // Định nghĩa các placeholder và giá trị tương ứng
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("Tên Người Nhận", "Test username");
        placeholders.put("Tiêu đề Bài viết", "Test tiêu đề");
        placeholders.put("Mô tả ngắn về bài viết", "Test content");
        placeholders.put("Link Bài viết", "testlink.com");
        placeholders.put("Tên Công ty", "Công ty XYZ");
        placeholders.put("Địa chỉ Công ty", "123 Đường ABC, Thành phố XYZ");
        placeholders.put("Liên hệ", "contact@xyz.com");

        try {
            String htmlContent = emailService.getTemplateContent("templates/new_article.html", placeholders);
            emailService.sendEmailHtml(to, subject, htmlContent);
            log.info("Email New Article sent to {}", to);
        } catch (IOException e) {
            log.error("Error reading email template", e);
        } catch (MessagingException e) {
            log.error("Error sending email", e);
        }
    }

}

