package com.windev.comment_service.mapper;

import com.windev.comment_service.dto.request.CreateReportRequest;
import com.windev.comment_service.dto.response.ReportDto;
import com.windev.comment_service.entity.Report;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ReportMapper {
    ReportMapper INSTANCE = Mappers.getMapper(ReportMapper.class);

    ReportDto toDto(Report report);

}
