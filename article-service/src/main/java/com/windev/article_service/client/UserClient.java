package com.windev.article_service.client;

import com.windev.article_service.dto.response.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "USER-SERVICE")
public interface UserClient {
    @GetMapping("/api/v1/auth/me")
    ResponseEntity<UserDto> getCurrentUser();

    @GetMapping("/api/v1/users/{userId}")
    ResponseEntity<UserDto> getAuthor(@PathVariable("userId") Long userId);

}
