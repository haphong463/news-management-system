package com.windev.article_service.service.tag;

import com.windev.article_service.dto.request.tag.CreateTagRequest;
import com.windev.article_service.dto.response.TagDto;
import com.windev.article_service.entity.Article;
import com.windev.article_service.entity.Tag;
import com.windev.article_service.exception.GlobalException;
import com.windev.article_service.mapper.TagMapper;
import com.windev.article_service.repository.ArticleRepository;
import com.windev.article_service.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    private final ArticleRepository articleRepository;

    private final TagMapper tagMapper;

    @Override
    @Transactional
    public TagDto createTag(CreateTagRequest createTagRequest) {
        Article article = articleRepository.findById(createTagRequest.getArticleId())
                .orElseThrow(() -> new GlobalException("Article with ID: " + createTagRequest.getArticleId() + " not found.", HttpStatus.NOT_FOUND));

        Set<Article> articles = new HashSet<>();
        articles.add(article);

        Tag tag = Tag.builder().name(createTagRequest.getName()).articles(articles).build();

        Tag savedTag = tagRepository.save(tag);
        return tagMapper.toDto(savedTag);
    }

    @Override
    public void deleteUnusedTags() {
        List<Tag> tags = tagRepository.findByArticlesIsEmpty();
        tagRepository.deleteAll(tags);
    }

    @Override
    public List<TagDto> getUnusedTags() {
        return tagRepository.findByArticlesIsEmpty()
                .stream().map(tagMapper::toDto)
                .collect(Collectors.toList());
    }
}
