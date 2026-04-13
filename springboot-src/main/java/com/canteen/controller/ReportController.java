package com.canteen.controller;

import com.canteen.common.result.Result;
import com.canteen.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('reports:view')")
    public Result<Map<String, Object>> getReportSummary(
            @RequestParam(defaultValue = "week") String viewMode) {
        return reportService.getSummary(viewMode);
    }

    @GetMapping("/employees")
    @PreAuthorize("hasAuthority('reports:view')")
    public Result<Map<String, Object>> getReportEmployees(
            @RequestParam(defaultValue = "week") String viewMode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int minScore) {
        return reportService.getEmployees(viewMode, page, pageSize, keyword, role, minScore);
    }

}
