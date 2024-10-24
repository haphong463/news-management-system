package com.windev.article_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCategoryRequest {
    @NotBlank(message = "Category name cannot be blank!")
    private String name;

    @NotBlank(message = "Category description cannot be blank!")
    private String description;

    //Optional
    private Long parentCategoryId;
}
