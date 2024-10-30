package com.windev.article_service.specification;

import com.windev.article_service.entity.Article;
import com.windev.article_service.entity.Category;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Set;

public class ArticleSpecification {
    public static Specification<Article> hasTitleContaining(String title){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Article> hasAuthorId(Long authorId){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("authorId"), authorId);
    }

    public static Specification<Article> hasCategoriesIn(Set<String> categories){
        return (root, query, criteriaBuilder) -> {
            if(categories == null || categories.isEmpty()){
                return null;
            }
            query.distinct(true);
            Join<Article, Category> categoryJoin = root.join("categories", JoinType.INNER);
            return categoryJoin.get("name").in(categories);
        };
    }

}
