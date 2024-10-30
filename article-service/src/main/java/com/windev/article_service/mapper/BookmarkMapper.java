package com.windev.article_service.mapper;

import com.windev.article_service.dto.response.BookmarkDto;
import com.windev.article_service.entity.Bookmark;
import com.windev.article_service.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {ArticleMapper.class})
public interface BookmarkMapper {

    @Mapping(source = "article", target = "article")
    BookmarkDto toDto(Bookmark bookmark);

    Bookmark toEntity(BookmarkDto bookmarkDto);
}
