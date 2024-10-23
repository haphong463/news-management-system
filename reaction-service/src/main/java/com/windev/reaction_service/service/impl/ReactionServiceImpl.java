package com.windev.reaction_service.service;

import com.windev.reaction_service.dto.ReactionDto;
import com.windev.reaction_service.entity.Reaction;
import com.windev.reaction_service.entity.ReactionType;
import com.windev.reaction_service.repository.ReactionRepository;
import com.windev.reaction_service.repository.ReactionTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReactionServiceImpl {

    private final ReactionRepository reactionRepository;
    private final ReactionTypeRepository reactionTypeRepository;

    // Thêm hoặc cập nhật reaction và xóa cache cũ
    @Transactional
    @CacheEvict(value = {"reactionsByArticle", "reactionsByComment"}, allEntries = true)
    public Reaction addOrUpdateReaction(ReactionDto reactionDto) {
        Optional<ReactionType> reactionTypeOpt = reactionTypeRepository.findById(reactionDto.getReactionTypeId());
        if (reactionTypeOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid reaction type");
        }

        ReactionType reactionType = reactionTypeOpt.get();
        Reaction reaction = null;

        // Kiểm tra reaction đã tồn tại trên bài viết hay bình luận chưa
        if (reactionDto.getArticleId() != null) {
            reaction = reactionRepository.findByUserIdAndArticleId(reactionDto.getUserId(), reactionDto.getArticleId())
                    .orElse(new Reaction());
        } else if (reactionDto.getCommentId() != null) {
            reaction = reactionRepository.findByUserIdAndCommentId(reactionDto.getUserId(), reactionDto.getCommentId())
                    .orElse(new Reaction());
        }

        // Cập nhật hoặc thêm mới reaction
        reaction.setUserId(reactionDto.getUserId());
        reaction.setArticleId(reactionDto.getArticleId());
        reaction.setCommentId(reactionDto.getCommentId());
        reaction.setReactionType(reactionType);

        return reactionRepository.save(reaction);
    }

    // Lấy tất cả reaction của bài viết và lưu kết quả vào cache
    @Cacheable(value = "reactionsByArticle", key = "#articleId")
    public List<Reaction> getReactionsByArticle(Long articleId) {
        return reactionRepository.findByArticleId(articleId);
    }

    // Lấy tất cả reaction của bình luận và lưu kết quả vào cache
    @Cacheable(value = "reactionsByComment", key = "#commentId")
    public List<Reaction> getReactionsByComment(Long commentId) {
        return reactionRepository.findByCommentId(commentId);
    }

    // Xóa reaction và xóa cache cũ
    @CacheEvict(value = {"reactionsByArticle", "reactionsByComment"}, allEntries = true)
    public void deleteReaction(Long reactionId) {
        reactionRepository.deleteById(reactionId);
    }
}
