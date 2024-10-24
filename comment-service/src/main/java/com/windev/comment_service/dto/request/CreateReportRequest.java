package com.windev.comment_service.dto.request;

import com.windev.comment_service.dto.response.CommentDto;
import lombok.Data;

@Data
public class CreateReportRequest {
    private Long commentId;

    private Long reporterUserId;

    private String reason;

}
