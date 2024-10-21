package com.windev.article_service.service.impl;

import com.github.slugify.Slugify;
import com.windev.article_service.client.UserClient;
import com.windev.article_service.dto.request.CreateArticleRequest;
import com.windev.article_service.dto.request.UpdateArticleRequest;
import com.windev.article_service.dto.response.ArticleDto;
import com.windev.article_service.dto.response.PaginatedResponseDto;
import com.windev.article_service.dto.response.UserDto;
import com.windev.article_service.entity.Article;
import com.windev.article_service.entity.Category;
import com.windev.article_service.exception.GlobalException;
import com.windev.article_service.mapper.ArticleMapper;
import com.windev.article_service.repository.ArticleRepository;
import com.windev.article_service.repository.CategoryRepository;
import com.windev.article_service.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;
    private final UserClient userClient;

    @Override
    public ArticleDto createArticle(CreateArticleRequest createArticleRequest) {

        log.info("createArticle() --> CREATE ARTICLE METHOD START");
        Article article = prepareNewArticle(createArticleRequest);

        Article savedArticle = articleRepository.save(article);
        log.info("createArticle() --> CREATE ARTICLE OK: {}", savedArticle.getTitle());
        return articleMapper.toDto(savedArticle);
    }

    @Override
    public PaginatedResponseDto<ArticleDto> getAllArticles(Pageable pageable) {
        return convertToPaginatedResponseDto(articleRepository.findAll(pageable));
    }

    @Override
    public PaginatedResponseDto<ArticleDto> searchArticlesByTitle(String title, Pageable pageable) {
        return convertToPaginatedResponseDto(articleRepository.findByTitleContainingIgnoreCase(title, pageable));
    }

    @Override
    public PaginatedResponseDto<ArticleDto> getArticlesByAuthor(Long authorId, Pageable pageable) {
        return convertToPaginatedResponseDto(articleRepository.findByAuthorId(authorId, pageable));
    }

    @Override
    public ArticleDto updateArticle(Long articleId, UpdateArticleRequest updateArticleRequest) {
        return null;
    }

    @Override
    public void deleteArticle(Long articleId) {

    }



    private PaginatedResponseDto<ArticleDto> convertToPaginatedResponseDto(Page<Article> articlePage) {
        List<ArticleDto> articles = articlePage.getContent().stream().map(articleMapper::toDto).toList();
        return new PaginatedResponseDto<>(articles, articlePage.getNumber(), articlePage.getSize(),
                articlePage.getTotalPages(), articlePage.getTotalElements(), articlePage.isLast());
    }

    //* PREPARE NEW ARTICLE
    private Article prepareNewArticle(CreateArticleRequest createArticleRequest) {
        final Slugify slugify = Slugify.builder().build();
        ResponseEntity<UserDto> response = userClient.getCurrentUser();
        Article article = new Article();
        article.setTitle(createArticleRequest.getTitle());
        article.setContent(createArticleRequest.getContent());
        article.setSlug(slugify.slugify(createArticleRequest.getTitle()));
        article.setAuthorId(response.getBody().getId());

        Set<Category> validatedCategories = new HashSet<>();
        for (String category : createArticleRequest.getCategoriesId()) {
            Category existingCategory = categoryRepository.findByName(category)
                    .orElseThrow(() -> {
                        log.warn("createArticle() --> CATEGORY NOT FOUND!");
                        return new GlobalException("Category not found: " + category, HttpStatus.NOT_FOUND);
                    });
            validatedCategories.add(existingCategory);
        }
        article.setCategories(validatedCategories);
        return article;
    }
}
