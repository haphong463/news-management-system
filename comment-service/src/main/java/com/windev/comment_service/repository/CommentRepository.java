package com.windev.comment_service.repository;

import com.windev.comment_service.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByArticleIdAndParentCommentIsNull(@Param("articleId") Long articleId, Pageable pageable);

    Page<Comment> findByUserId(Long userId, Pageable pageable);
}
