package com.windev.comment_service.dto.response;

import com.windev.comment_service.enums.ReportStatus;
import lombok.Data;

@Data
public class ReportDto {

    private Long id;

    private CommentDto comment;

    private Long reporterUserId;

    private String reason;

    private ReportStatus status;
}
