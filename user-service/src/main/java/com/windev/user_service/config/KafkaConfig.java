package com.windev.user_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic contentEventsTopic() {
        return new NewTopic("UserEvents", 1, (short) 1);
    }
}