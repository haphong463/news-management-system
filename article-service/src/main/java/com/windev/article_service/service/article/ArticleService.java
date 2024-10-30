package com.windev.article_service.service.article;

import com.windev.article_service.dto.request.article.CreateArticleRequest;
import com.windev.article_service.dto.request.article.UpdateArticleRequest;
import com.windev.article_service.dto.response.ArticleDto;
import com.windev.article_service.dto.response.PaginatedResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface ArticleService {
    ArticleDto createArticle(CreateArticleRequest createArticleRequest);

    PaginatedResponseDto<ArticleDto> getAllArticles(Pageable pageable);

    PaginatedResponseDto<ArticleDto> searchArticles(String title, Long authorId, Set<String> categories, Pageable pageable);

    ArticleDto getArticleById(Long articleId);

    ArticleDto updateArticle(Long articleId, UpdateArticleRequest updateArticleRequest);

    String deleteArticle(Long articleId);

    String crawlNewsData();


}

