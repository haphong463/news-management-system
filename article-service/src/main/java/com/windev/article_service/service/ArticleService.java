package com.windev.article_service.service;

import com.windev.article_service.dto.request.CreateArticleRequest;
import com.windev.article_service.dto.request.UpdateArticleRequest;
import com.windev.article_service.dto.response.ArticleDto;
import com.windev.article_service.dto.response.PaginatedResponseDto;
import org.springframework.data.domain.Pageable;

public interface ArticleService {
    ArticleDto createArticle(CreateArticleRequest createArticleRequest);

    PaginatedResponseDto<ArticleDto> getAllArticles(Pageable pageable);

    PaginatedResponseDto<ArticleDto> searchArticlesByTitle(String title, Pageable pageable);

    PaginatedResponseDto<ArticleDto> searchArticlesByAuthor(Long authorId, Pageable pageable);

    ArticleDto getArticleById(Long articleId);

    ArticleDto updateArticle(Long articleId, UpdateArticleRequest updateArticleRequest);

    void deleteArticle(Long articleId);

    String crawlNewsData();
}

