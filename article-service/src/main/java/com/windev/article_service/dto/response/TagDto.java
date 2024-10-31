package com.windev.article_service.dto.response;

import lombok.Data;

import java.util.Set;

@Data
public class TagDto {
    private String name;
    private Set<ArticleDto> articles;
}
