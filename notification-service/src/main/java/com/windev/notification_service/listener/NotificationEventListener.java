package com.windev.notification_service.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.windev.notification_service.event.EventMessage;
import com.windev.notification_service.event.PasswordResetEvent;
import com.windev.notification_service.event.UserRegisteredEvent;
import com.windev.notification_service.handler.NotificationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationEventListener {
    private final Map<String, NotificationStrategy> strategies;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "notifications", groupId = "notification-service-group")
    public void handleEvents(@Payload String message) {
        try {
            EventMessage event = objectMapper.readValue(message, EventMessage.class);

            String eventType = event.getEventType();
            Object data = event.getData();

            NotificationStrategy strategy = strategies.get(eventType + "-strategy");
            if (strategy != null) {
                Object deserializedData = deserializeData(eventType, data);
                strategy.sendNotification(deserializedData);
                log.info("handleEvents() --> Đã xử lý sự kiện: {}", eventType);
            }else{
                log.warn("handleEvents() --> Không tìm thấy chiến lược cho loại sự kiện: {}", eventType);
            }

        } catch (Exception e) {
            log.error("handleEvents() --> Error handling event: {}", e.getMessage(), e);
        }
    }

    private Object deserializeData(String eventType, Object data) throws JsonProcessingException {
        switch (eventType) {
            case "user-registered":
                return objectMapper.convertValue(data, UserRegisteredEvent.class);
            case "password-reset":
                return objectMapper.convertValue(data, PasswordResetEvent.class);
            // Add cases for other event types here
            default:
                throw new IllegalArgumentException("Unknown event type: " + eventType);
        }
    }

}
