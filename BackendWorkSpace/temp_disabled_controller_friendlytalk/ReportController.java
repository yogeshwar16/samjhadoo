package com.samjhadoo.controller.api.friendlytalk;

import com.samjhadoo.dto.friendlytalk.ReportDTO.ReportRequestDTO;
import com.samjhadoo.dto.friendlytalk.ReportDTO.ReportResponseDTO;
import com.samjhadoo.dto.friendlytalk.ReportDTO.ReportReviewDTO;
import com.samjhadoo.dto.friendlytalk.ReportDTO.ReportStatisticsDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.friendlytalk.UserReport;
import com.samjhadoo.security.CurrentUser;
import com.samjhadoo.security.UserPrincipal;
import com.samjhadoo.service.friendlytalk.ReportService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity<ReportResponseDTO> submitReport(
            @Valid @RequestBody ReportRequestDTO request,
            @CurrentUser UserPrincipal currentUser) {
        
        UserReport report = reportService.submitReport(request, currentUser.getUser());
        return ResponseEntity.ok(toReportResponseDTO(report));
    }

    @GetMapping
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<Page<ReportResponseDTO>> getReports(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<UserReport> reports = reportService.getReports(pageable);
        return ResponseEntity.ok(reports.map(this::toReportResponseDTO));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR') || @reportSecurity.isReporter(#id, #currentUser)")
    public ResponseEntity<ReportResponseDTO> getReport(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        
        UserReport report = reportService.getReport(id);
        return ResponseEntity.ok(toReportResponseDTO(report));
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ReportResponseDTO> reviewReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportReviewDTO review,
            @CurrentUser UserPrincipal currentUser) {
        
        UserReport report = reportService.reviewReport(id, currentUser.getId().toString(), 
                review.getAction(), review.getNotes());
        return ResponseEntity.ok(toReportResponseDTO(report));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ReportStatisticsDTO> getReportStatistics() {
        return ResponseEntity.ok(reportService.getReportStatistics());
    }

    // Helper method to convert entity to DTO
    private ReportResponseDTO toReportResponseDTO(UserReport report) {
        return modelMapper.map(report, ReportResponseDTO.class);
    }
}
