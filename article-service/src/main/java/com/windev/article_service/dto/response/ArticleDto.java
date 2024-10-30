package com.windev.article_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleDto {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private Date createdAt;
    private Date updatedAt;
    private List<String> categories;
    private String mainImage;
    private String thumbnailImage;

    public String getMainImage() {
        if (mainImage != null) {
            return "/uploads/" + mainImage;
        }
        return null;
    }

    public String getThumbnailImage() {
        if (thumbnailImage != null) {
            return "/uploads/" + thumbnailImage;
        }
        return null;
    }
}
