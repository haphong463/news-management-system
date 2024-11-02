package com.windev.notification_service.handler;

import com.windev.notification_service.event.NewCommentEvent;
import com.windev.notification_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service("new-comment-strategy")
@RequiredArgsConstructor
@Slf4j
public class NewCommentStrategy implements NotificationStrategy {

    private final EmailService emailService;


    @Override
    public void sendNotification(Object data) {
        // gui email bao co comment moi cho tac gia
        if(!(data instanceof NewCommentEvent)){
            log.error("Invalid data type for NewCommentEvent");
            return;
        }

        NewCommentEvent commentEvent = (NewCommentEvent) data;
        log.info("NewCommentStrategy --> received event from comment service: {}", data);


    }
}
