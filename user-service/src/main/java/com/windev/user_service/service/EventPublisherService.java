package com.windev.user_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.windev.user_service.constant.EventConstant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javaguides.common_lib.EventMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Publishes an event to the Kafka notifications topic.
     *
     * @param eventType The type of the event.
     * @param data      The event-specific data.
     */
    public void publishEvent(String eventType, Object data) {
        EventMessage eventMessage = EventMessage.builder()
                .eventType(eventType)
                .data(data)
                .build();
        try {
            String eventAsString = objectMapper.writeValueAsString(eventMessage);
            kafkaTemplate.send(EventConstant.NOTIFICATIONS_TOPIC, eventAsString);
            log.info("publishEvent() --> Event sent successfully: {}", eventAsString);
        } catch (JsonProcessingException e) {
            log.error("publishEvent() --> Failed to serialize event: {}", eventMessage, e);
        } catch (Exception e) {
            log.error("publishEvent() --> Failed to send event to Kafka", e);
        }
    }
}