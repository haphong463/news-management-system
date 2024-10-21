package com.windev.article_service.mapper;

import com.windev.article_service.dto.response.ArticleDto;
import com.windev.article_service.entity.Article;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface ArticleMapper {
    ArticleMapper INSTANCE = Mappers.getMapper(ArticleMapper.class);
    ArticleDto toDto(Article article);
    Article toEntity(ArticleDto articleDTO);
}
