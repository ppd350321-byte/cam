package com.canteen.service;

import com.canteen.common.result.Result;

import java.util.Map;

public interface ReportService {

    Result<Map<String, Object>> getSummary(String viewMode);

    Result<Map<String, Object>> getEmployees(String viewMode, int page, int pageSize, String keyword, String role, int minScore);
}
