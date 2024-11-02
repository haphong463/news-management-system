package com.windev.notification_service.event;

import lombok.Data;

@Data
public class NewCommentEvent {
    private Long articleId;
    private String content;
}
