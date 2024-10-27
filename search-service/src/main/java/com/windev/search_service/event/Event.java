package com.windev.search_service.event;

import lombok.Data;

import java.util.Set;

@Data
public class Event {
    private Long contentId;
    private String title;
    private String action;
    private Long authorId;
    private Set<String> categories;
}
