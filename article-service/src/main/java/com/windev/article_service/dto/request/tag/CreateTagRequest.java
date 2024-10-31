package com.windev.article_service.dto.request.tag;

import lombok.Data;

@Data
public class CreateTagRequest {
    private String name;
    private Long articleId;
}
