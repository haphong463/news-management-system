package com.windev.reaction_service.repository;

import com.windev.reaction_service.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    List<Reaction> findByArticleId(Long articleId);

    List<Reaction> findByCommentId(Long commentId);

    Optional<Reaction> findByUserIdAndArticleId(Long userId, Long articleId);

    Optional<Reaction> findByUserIdAndCommentId(Long userId, Long commentId);

    List<Reaction> findByCommentIdIn(List<Long> commentIds);
}
