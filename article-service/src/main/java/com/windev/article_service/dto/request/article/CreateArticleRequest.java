package com.windev.article_service.dto.request.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Data
public class CreateArticleRequest {
    @NotBlank
    @Length(min = 10, max = 200, message = "Title must be between 10 and 200 characters long.")
    private String title;

    @NotBlank
    @Length(min = 50, message = "Content must be longer than 50 characters.")
    private String content;

    private Set<String> categoryNames;

    private Set<String> tags;

    @NotNull(message = "File cannot be null.")
    private MultipartFile mainImage;
}
