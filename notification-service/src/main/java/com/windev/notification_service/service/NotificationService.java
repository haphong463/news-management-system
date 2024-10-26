package com.windev.notification_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.windev.notification_service.client.UserClient;
import com.windev.notification_service.dto.UserDto;
import com.windev.notification_service.event.ContentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserClient userClient; // Feign Client

    @Autowired
    private ObjectMapper objectMapper; // JSON deserializer

    public void handleContentEvent(String message) throws JsonProcessingException {
        ContentEvent contentEvent = objectMapper.readValue(message, ContentEvent.class);

        switch (contentEvent.getAction()) {
            case "CREATED":
                handleArticleCreated(contentEvent);
                break;
            case "UPDATED":
//                handleArticleUpdated(contentEvent);
                break;
            case "DELETED":
//                handleArticleDeleted(contentEvent);
                break;
            default:
                // Hành động không xác định
                break;
        }
    }

    private void handleArticleCreated(ContentEvent event) {
        // Lấy danh sách người dùng quan tâm
        List<UserDto> subscribers = userClient.getSubscribers();

        String subject = "Bài viết mới: " + event.getTitle();
        String body = "Một bài viết mới đã được xuất bản. Hãy đọc ngay!";

        // Gửi email
        subscribers.forEach(user -> {
            emailService.sendEmail(user.getEmail(), subject, body);
        });
    }

    // Các phương thức xử lý khác...
}
