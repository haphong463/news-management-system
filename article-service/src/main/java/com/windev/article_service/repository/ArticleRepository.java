package com.windev.article_service.repository;

import com.windev.article_service.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {
    Page<Article> findByAuthorId(Long authorId, Pageable pageable);
    Page<Article> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Article> findByCategories_Id(Long categoryId, Pageable pageable);

    @Override
    Page<Article> findAll(Pageable pageable);

}
