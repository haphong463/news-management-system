package com.windev.search_service.repository;

import com.windev.search_service.model.ArticleDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ArticleDocumentRepository extends ElasticsearchRepository<ArticleDocument, Long> {
    List<ArticleDocument> findByTitleContainingIgnoreCase(String title);

    List<ArticleDocument> findByAuthorId(Long authorId);

    List<ArticleDocument> findByCategoriesIn(Set<String> categories);

    List<ArticleDocument> findByTitleContainingIgnoreCaseAndAuthorId(String title, Long authorId);

    List<ArticleDocument> findByTitleContainingIgnoreCaseAndAuthorIdAndCategoriesIn(String title, Long authorId, Set<String> categories);
}
