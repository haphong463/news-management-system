package com.windev.article_service.service;

import com.windev.article_service.dto.request.CreateCategoryRequest;
import com.windev.article_service.dto.request.UpdateCategoryRequest;
import com.windev.article_service.dto.response.CategoryDto;
import com.windev.article_service.dto.response.PaginatedResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    CategoryDto addCategory(CreateCategoryRequest createCategoryRequest);
    PaginatedResponseDto<CategoryDto> getAllCategory(Pageable pageable);
    void deleteCategory(Long id);
    CategoryDto updateCategory(Long id, UpdateCategoryRequest updateCategoryRequest);
    CategoryDto getCategory(Long id);
}
