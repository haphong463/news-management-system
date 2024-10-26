package com.windev.article_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentEvent {
    private Long contentId;
    private String title;
    private String action; // "CREATED", "UPDATED", "DELETED"
    private Long authorId;
    // Getters và Setters
}
