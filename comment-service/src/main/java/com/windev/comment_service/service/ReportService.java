package com.windev.comment_service.service;

import com.windev.comment_service.dto.request.CreateReportRequest;
import com.windev.comment_service.dto.response.ReportDto;
import com.windev.comment_service.entity.Report;
import com.windev.comment_service.enums.ReportStatus;
import com.windev.comment_service.payload.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReportService {
    ReportDto createReport(CreateReportRequest createReportRequest);

    PaginatedResponse<ReportDto> getAllReports(Pageable pageable);

    PaginatedResponse<ReportDto> getReportsByStatus(ReportStatus status, Pageable pageable);

    ReportDto updateReportStatus(Long reportId, ReportStatus status);

}
