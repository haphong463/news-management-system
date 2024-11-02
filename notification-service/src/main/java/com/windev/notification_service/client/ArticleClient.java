package com.windev.notification_service.client;

import com.windev.notification_service.dto.ArticleDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "article-service")
public interface ArticleClient {
    @GetMapping("/api/v1/articles/{articleId}")
    ResponseEntity<ArticleDto> getArticleById(@PathVariable("articleId") Long articleId);
}
