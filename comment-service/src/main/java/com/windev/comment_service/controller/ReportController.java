package com.windev.comment_service.controller;

import com.windev.comment_service.dto.request.CreateReportRequest;
import com.windev.comment_service.dto.response.ReportDto;
import com.windev.comment_service.exception.GlobalException;
import com.windev.comment_service.payload.response.PaginatedResponse;
import com.windev.comment_service.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@RestController
public class ReportController {

    private final ReportService reportService;


    @GetMapping
    public ResponseEntity<?> getAllReports(@RequestParam(defaultValue = "10") int size,
                                           @RequestParam(defaultValue = "0") int page){
        try {
            Pageable pageable = PageRequest.of(page, size);
            PaginatedResponse<ReportDto>  response = reportService.getAllReports(pageable);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch(Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody CreateReportRequest createReportRequest){
        try {
            ReportDto response = reportService.createReport(createReportRequest);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch(GlobalException e){
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
        catch(Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
