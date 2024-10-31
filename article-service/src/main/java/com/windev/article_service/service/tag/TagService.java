package com.windev.article_service.service.tag;

import com.windev.article_service.dto.request.tag.CreateTagRequest;
import com.windev.article_service.dto.response.TagDto;

import java.util.List;

public interface TagService {
    TagDto createTag(CreateTagRequest createTagRequest);

    void deleteUnusedTags();

    List<TagDto> getUnusedTags();
}
