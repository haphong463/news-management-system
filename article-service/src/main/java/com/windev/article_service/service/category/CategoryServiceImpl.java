package com.windev.article_service.service.category;

import com.windev.article_service.dto.request.category.CreateCategoryRequest;
import com.windev.article_service.dto.request.category.UpdateCategoryRequest;
import com.windev.article_service.dto.response.CategoryDto;
import com.windev.article_service.dto.response.PaginatedResponseDto;
import com.windev.article_service.entity.Category;
import com.windev.article_service.exception.GlobalException;
import com.windev.article_service.mapper.CategoryMapper;
import com.windev.article_service.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDto addCategory(CreateCategoryRequest createCategoryRequest) {

        Category category = new Category();
        category.setName(createCategoryRequest.getName());
        category.setDescription(createCategoryRequest.getDescription());

        if (createCategoryRequest.getParentCategoryId() != null) {
            Long parentId = createCategoryRequest.getParentCategoryId();
            Category existingParentCategory = categoryRepository
                    .findById(parentId)
                    .orElseThrow(() -> new GlobalException("Not found category Id: " + parentId, HttpStatus.NOT_FOUND));

            category.setParentCategory(existingParentCategory);
        }

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    public PaginatedResponseDto<CategoryDto> getAllCategory(Pageable pageable) {
        Page<Category> categoryPage = categoryRepository.findByParentCategoryIsNull(pageable);

        List<CategoryDto> list = categoryPage.stream().map(categoryMapper::toDto).toList();

        return new PaginatedResponseDto<>(
                list,
                categoryPage.getNumber(),
                categoryPage.getSize(),
                categoryPage.getTotalPages(),
                categoryPage.getTotalElements(),
                categoryPage.isLast());
    }

    @Override
    public void deleteCategory(Long id) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new GlobalException("Not found category ID: " + id, HttpStatus.NOT_FOUND));

        categoryRepository.delete(existingCategory);
    }

    @Override
    public CategoryDto updateCategory(Long id, UpdateCategoryRequest updateCategoryRequest) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new GlobalException("Not found category ID: " + id, HttpStatus.NOT_FOUND));

        existingCategory.setName(updateCategoryRequest.getName());
        existingCategory.setDescription(updateCategoryRequest.getDescription());

        if (updateCategoryRequest.getParentCategoryId() != null) {
            Long parentId = updateCategoryRequest.getParentCategoryId();
            Category existingParentCategory = categoryRepository
                    .findById(parentId)
                    .orElseThrow(() -> new GlobalException("Not found category Id: " + parentId, HttpStatus.NOT_FOUND));

            existingCategory.setParentCategory(existingParentCategory);
        }

        Category updatedCategory = categoryRepository.save(existingCategory);

        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    public CategoryDto getCategory(Long id) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new GlobalException("Not found category ID: " + id, HttpStatus.NOT_FOUND));
        return categoryMapper.toDto(existingCategory);
    }



}
