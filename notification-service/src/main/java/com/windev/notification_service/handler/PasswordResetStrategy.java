package com.windev.notification_service.handler;

import com.windev.notification_service.event.PasswordResetEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service("password-reset-strategy")
@Slf4j
public class PasswordResetStrategy implements NotificationStrategy{
    @Override
    public void sendNotification(Object data) {
        if (!(data instanceof PasswordResetEvent)) {
            throw new IllegalArgumentException("Invalid data type for PasswordResetStrategy");
        }
        PasswordResetEvent event = (PasswordResetEvent) data;
        log.info("--> Send an announcement for password reset: {}", event);
    }
}
