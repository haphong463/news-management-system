package com.windev.notification_service.handler;

import com.windev.notification_service.event.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service("user-registered-strategy")
@Slf4j
public class UserRegisteredStrategy implements NotificationStrategy {
    @Override
    public void sendNotification(Object data) {
        if (!(data instanceof UserRegisteredEvent)) {
            throw new IllegalArgumentException("Invalid data type for UserRegisteredStrategy");
        }
        UserRegisteredEvent event = (UserRegisteredEvent) data;
        log.info("--> Send an announcement for user registered: {}", event);

        // Implement the notification logic here
    }
}
