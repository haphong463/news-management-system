package com.windev.article_service.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCategoryRequest {
    @NotBlank(message = "Category name cannot be blank!")
    private String name;

    @NotBlank(message = "Category description cannot be blank!")
    private String description;

    //Optional
    private Long parentCategoryId;
}
