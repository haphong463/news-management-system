package com.windev.comment_service.repository;

import com.windev.comment_service.entity.Report;
import com.windev.comment_service.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByCommentId(Long commentId, Pageable pageable);
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    @Override
    Page<Report> findAll(Pageable pageable);
}
