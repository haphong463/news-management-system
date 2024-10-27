package com.windev.article_service.mapper;

import com.windev.article_service.dto.response.ArticleDto;
import com.windev.article_service.entity.Article;
import com.windev.article_service.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ArticleMapper {

    @Mapping(source = "categories", target = "categories", qualifiedByName = "categoriesToStrings")
    ArticleDto toDto(Article article);


    @Mapping(source = "categories", target = "categories", qualifiedByName = "stringsToCategories")
    Article toEntity(ArticleDto articleDTO);

    @Named("stringsToCategories")
    default List<Category> stringsToCategories(List<String> categoryNames) {
        if (categoryNames == null) {
            return null;
        }
        return categoryNames.stream()
                .map(name -> {
                    Category category = new Category();
                    category.setName(name);
                    return category;
                })
                .collect(Collectors.toList());
    }
    @Named("categoriesToStrings")
    default List<String> categoriesToStrings(List<Category> categories) {
        if (categories == null) {
            return null;
        }
        return categories.stream().map(Category::getName).collect(Collectors.toList());
    }

}
