package com.windev.reaction_service.controller;

import com.windev.reaction_service.dto.ReactionDto;
import com.windev.reaction_service.entity.Reaction;
import com.windev.reaction_service.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reactions")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;

    @PostMapping
    public ResponseEntity<Reaction> addOrUpdateReaction(@RequestBody ReactionDto reactionDto) {
        return ResponseEntity.ok(reactionService.addOrUpdateReaction(reactionDto));
    }

    @GetMapping("/article/{articleId}")
    public ResponseEntity<List<Reaction>> getReactionsByArticle(@PathVariable Long articleId) {
        return ResponseEntity.ok(reactionService.getReactionsByArticle(articleId));
    }

    @GetMapping("/comment/{commentId}")
    public ResponseEntity<List<Reaction>> getReactionsByComment(@PathVariable Long commentId) {
        return ResponseEntity.ok(reactionService.getReactionsByComment(commentId));
    }

    @DeleteMapping("/{reactionId}")
    public ResponseEntity<Void> deleteReaction(@PathVariable Long reactionId) {
        reactionService.deleteReaction(reactionId);
        return ResponseEntity.noContent().build();
    }
}
