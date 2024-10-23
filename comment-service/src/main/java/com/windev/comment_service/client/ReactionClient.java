package com.windev.comment_service.client;

import com.windev.comment_service.dto.response.ReactionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "REACTION-SERVICE")
public interface ReactionClient {
    @GetMapping("/api/v1/reactions/comments")
    ResponseEntity<List<ReactionDto>> getReactionsByComments(@RequestParam List<Long> commentIds);
}
