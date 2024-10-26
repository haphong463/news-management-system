package com.windev.notification_service.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.windev.notification_service.event.ContentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ContentEventListener {

    @KafkaListener(topics = "ContentEvents", groupId = "notification-service-group")
    public void listenContentEvents(@Payload String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ContentEvent event = objectMapper.readValue(message, ContentEvent.class);
            // Xử lý sự kiện
            handleContentEvent(event);
        } catch (Exception e) {
            // Xử lý lỗi
            e.printStackTrace();
        }
    }

    private void handleContentEvent(ContentEvent event) {
        log.info("received content --> {}", event.toString());
        // Logic xử lý sự kiện
    }
}
