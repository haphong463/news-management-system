package com.windev.article_service.mapper;


import com.windev.article_service.dto.response.CategoryDto;
import com.windev.article_service.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    CategoryDto toDto(Category category);
    Category toEntity(CategoryDto categoryDTO);
}
