package com.windev.notification_service.handler;

import com.windev.notification_service.event.NewArticleEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service("new-article-strategy")
@Slf4j
public class NewArticleStrategy implements  NotificationStrategy{
    @Override
    public void sendNotification(Object data) {
        NewArticleEvent event = (NewArticleEvent) data;

        log.info("--> Send an announcement for new article: {}", data);
    }
}
