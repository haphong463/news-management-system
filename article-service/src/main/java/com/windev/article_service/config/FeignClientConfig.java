package com.windev.article_service.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.http.HttpServletRequest;

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
