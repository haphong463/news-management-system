package com.windev.notification_service.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.windev.notification_service.event.ContentEvent;
import com.windev.notification_service.event.UserEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationEventListener {

    @KafkaListener(topics = "notifications", groupId = "notification-service-group")
    public void listenContentEvents(@Payload String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            UserEvent event = objectMapper.readValue(message, UserEvent.class);
            // Xử lý sự kiện
            handleUserEvent(event);
        } catch (Exception e) {
            // Xử lý lỗi
            e.printStackTrace();
        }
    }

    private void handleUserEvent(UserEvent event) {
        log.info("received content --> {}", event.toString());
        // Logic xử lý sự kiện
    }
}
