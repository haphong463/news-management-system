package com.windev.article_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

@Data
public class UpdateArticleRequest {
    @NotBlank
    @Length(min = 10, max = 200, message = "Title must be between 10 and 200 characters long.")
    private String title;

    @NotBlank
    @Length(min = 50, message = "Content must be longer than 50 characters.")
    private String content;

    private Set<String> categoriesId;
}
