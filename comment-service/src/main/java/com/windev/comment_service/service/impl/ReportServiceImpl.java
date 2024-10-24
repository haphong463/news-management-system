package com.windev.comment_service.service.impl;

import com.windev.comment_service.dto.request.CreateReportRequest;
import com.windev.comment_service.dto.response.ReportDto;
import com.windev.comment_service.entity.Comment;
import com.windev.comment_service.entity.Report;
import com.windev.comment_service.enums.ReportStatus;
import com.windev.comment_service.exception.GlobalException;
import com.windev.comment_service.mapper.ReportMapper;
import com.windev.comment_service.payload.response.PaginatedResponse;
import com.windev.comment_service.repository.CommentRepository;
import com.windev.comment_service.repository.ReportRepository;
import com.windev.comment_service.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;

    private final CommentRepository commentRepository;

    private final ReportMapper reportMapper;


    @Override
    public ReportDto createReport(CreateReportRequest createReportRequest) {
        Comment comment = commentRepository.findById(createReportRequest.getCommentId())
                .orElseThrow(() -> new GlobalException("Comment not found with id: " + createReportRequest.getCommentId(),HttpStatus.NOT_FOUND));

        Report report = new Report();
        report.setReporterUserId(createReportRequest.getReporterUserId());
        report.setReason(createReportRequest.getReason());
        report.setComment(comment);
        report.setStatus(ReportStatus.PENDING);


        Report savedReport = reportRepository.save(report);

        return reportMapper.toDto(savedReport);
    }

    @Override
    public PaginatedResponse<ReportDto> getAllReports(Pageable pageable) {
        Page<Report> reportPage = reportRepository.findAll(pageable);

        List<ReportDto> list = reportPage
                .getContent()
                .stream()
                .map(reportMapper::toDto)
                .toList();

        return new PaginatedResponse<>(list, reportPage.getNumber(), reportPage.getSize(), reportPage.getTotalPages(), reportPage.getTotalElements(), reportPage.isLast());
    }

    @Override
    public PaginatedResponse<ReportDto> getReportsByStatus(ReportStatus status, Pageable pageable) {
        Page<Report> reportPage = reportRepository.findByStatus(status, pageable);

        List<ReportDto> list = reportPage
                .getContent()
                .stream()
                .map(reportMapper::toDto)
                .toList();

        return new PaginatedResponse<>(list, reportPage.getNumber(), reportPage.getSize(), reportPage.getTotalPages(), reportPage.getTotalElements(), reportPage.isLast());
    }

    @Override
    public ReportDto updateReportStatus(Long reportId, ReportStatus status) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new GlobalException("Report not found with id " + reportId, HttpStatus.NOT_FOUND));
        report.setStatus(status);
        Report updatedReport = reportRepository.save(report);
        return reportMapper.toDto(updatedReport);
    }
}
