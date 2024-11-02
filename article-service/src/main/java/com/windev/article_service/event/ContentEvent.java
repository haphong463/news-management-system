package com.windev.article_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContentEvent {
    private Long contentId;
    private String title;
    private Long authorId;
    private String email;
    private String firstName;
    private String lastName;
    private String slug;
}
