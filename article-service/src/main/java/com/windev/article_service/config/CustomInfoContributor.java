package com.windev.article_service.config;


import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        // Thêm thông tin tùy chỉnh
        builder.withDetail("name", "Article Service");
        builder.withDetail("description", "Article Service for News Microservices");
        builder.withDetail("version", "1.0.0");
        // Thêm thông tin động, ví dụ: thời gian hiện tại
        builder.withDetail("currentTime", LocalDateTime.now().toString());
    }
}