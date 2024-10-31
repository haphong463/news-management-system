package com.windev.article_service.repository;

import com.windev.article_service.entity.Article;
import com.windev.article_service.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByArticlesIsEmpty();
}
