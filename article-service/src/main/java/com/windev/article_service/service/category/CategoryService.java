package com.windev.article_service.service.category;

import com.windev.article_service.dto.request.category.CreateCategoryRequest;
import com.windev.article_service.dto.request.category.UpdateCategoryRequest;
import com.windev.article_service.dto.response.CategoryDto;
import com.windev.article_service.dto.response.PaginatedResponseDto;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    CategoryDto addCategory(CreateCategoryRequest createCategoryRequest);
    PaginatedResponseDto<CategoryDto> getAllCategory(Pageable pageable);
    void deleteCategory(Long id);
    CategoryDto updateCategory(Long id, UpdateCategoryRequest updateCategoryRequest);
    CategoryDto getCategory(Long id);
}
