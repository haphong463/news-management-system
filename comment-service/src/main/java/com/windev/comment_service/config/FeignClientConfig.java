package com.windev.comment_service.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

    @Autowired
    private HttpServletRequest request;

    @Bean
    public RequestInterceptor requestTokenBearerInterceptor() {
        return requestTemplate -> {
            String authorizationToken = request.getHeader("Authorization");
            if (authorizationToken != null) {
                requestTemplate.header("Authorization", authorizationToken);
            }
        };
    }
}
