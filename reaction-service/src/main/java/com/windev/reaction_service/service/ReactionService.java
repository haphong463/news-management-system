package com.windev.reaction_service.service;

import com.windev.reaction_service.dto.ReactionDto;
import com.windev.reaction_service.entity.Reaction;

import java.util.List;

public interface ReactionService {
    Reaction addOrUpdateReaction(ReactionDto reactionDto);

    List<Reaction> getReactionsByArticle(Long articleId);

    List<Reaction> getReactionsByComment(Long commentId);

    void deleteReaction(Long reactionId);
}
