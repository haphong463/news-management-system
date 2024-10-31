package com.windev.article_service.mapper;

import com.windev.article_service.dto.response.TagDto;
import com.windev.article_service.entity.Tag;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ArticleMapper.class})
public interface TagMapper {
    TagDto toDto(Tag tag);
    Tag toEntity(TagDto tagDto);
}
