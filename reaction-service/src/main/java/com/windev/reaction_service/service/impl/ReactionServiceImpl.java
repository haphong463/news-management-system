package com.windev.reaction_service.service.impl;

import com.windev.reaction_service.dto.ReactionDto;
import com.windev.reaction_service.entity.Reaction;
import com.windev.reaction_service.entity.ReactionType;
import com.windev.reaction_service.repository.ReactionRepository;
import com.windev.reaction_service.repository.ReactionTypeRepository;
import com.windev.reaction_service.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ReactionServiceImpl implements ReactionService {

    private final ReactionRepository reactionRepository;
    private final ReactionTypeRepository reactionTypeRepository;
    private final RedisTemplate<String, Object> redisTemplate;  // Inject RedisTemplate

    private static final String REACTION_BY_ARTICLE_KEY = "reactionsByArticle::";
    private static final String REACTION_BY_COMMENT_KEY = "reactionsByComment::";

    // Thêm hoặc cập nhật reaction và xóa cache thủ công
    @Transactional
    public Reaction addOrUpdateReaction(ReactionDto reactionDto) {
        Optional<ReactionType> reactionTypeOpt = reactionTypeRepository.findById(reactionDto.getReactionTypeId());
        if (reactionTypeOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid reaction type");
        }

        ReactionType reactionType = reactionTypeOpt.get();
        Reaction reaction = new Reaction();

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

        Reaction savedReaction = reactionRepository.save(reaction);

        // Xóa cache cũ sau khi cập nhật
        if (reactionDto.getArticleId() != null) {
            redisTemplate.delete(REACTION_BY_ARTICLE_KEY + reactionDto.getArticleId());
        } else if (reactionDto.getCommentId() != null) {
            redisTemplate.delete(REACTION_BY_COMMENT_KEY + reactionDto.getCommentId());
        }

        return savedReaction;
    }

    // Lấy tất cả reaction của bài viết và lưu vào Redis nếu chưa có
    public List<Reaction> getReactionsByArticle(Long articleId) {
        String redisKey = REACTION_BY_ARTICLE_KEY + articleId;

        // Kiểm tra Redis cache trước
        List<Reaction> reactions = (List<Reaction>) redisTemplate.opsForValue().get(redisKey);
        if (reactions != null) {
            return reactions;
        }

        // Nếu không có trong cache, truy vấn từ DB và lưu vào Redis
        reactions = reactionRepository.findByArticleId(articleId);

        redisTemplate.opsForValue().set(redisKey, reactions, 10, TimeUnit.MINUTES);  // Cache 10 phút

        return reactions;
    }

    // Lấy tất cả reaction của bình luận và lưu vào Redis nếu chưa có
    public List<Reaction> getReactionsByComment(Long commentId) {
        String redisKey = REACTION_BY_COMMENT_KEY + commentId;

        // Kiểm tra Redis cache trước
        List<Reaction> reactions = (List<Reaction>) redisTemplate.opsForValue().get(redisKey);
        if (reactions != null) {
            return reactions;
        }

        // Nếu không có trong cache, truy vấn từ DB và lưu vào Redis
        reactions = reactionRepository.findByCommentId(commentId);
        redisTemplate.opsForValue().set(redisKey, reactions, 10, TimeUnit.MINUTES);  // Cache 10 phút

        return reactions;
    }

    // Xóa reaction và xóa cache thủ công
    public void deleteReaction(Long reactionId) {
        Optional<Reaction> reactionOpt = reactionRepository.findById(reactionId);
        if (reactionOpt.isPresent()) {
            Reaction reaction = reactionOpt.get();

            // Xóa cache cũ sau khi xóa reaction
            if (reaction.getArticleId() != null) {
                redisTemplate.delete(REACTION_BY_ARTICLE_KEY + reaction.getArticleId());
            } else if (reaction.getCommentId() != null) {
                redisTemplate.delete(REACTION_BY_COMMENT_KEY + reaction.getCommentId());
            }

            // Xóa reaction khỏi DB
            reactionRepository.deleteById(reactionId);
        }
    }

    @Override
    public List<Reaction> getReactionsByComments(List<Long> commentIds) {
        return reactionRepository.findByCommentIdIn(commentIds);
    }
}
