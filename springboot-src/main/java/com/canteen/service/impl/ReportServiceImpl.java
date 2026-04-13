package com.canteen.service.impl;

import com.canteen.common.result.Result;
import com.canteen.entity.Admin;
import com.canteen.entity.Dish;
import com.canteen.repository.AdminPerformanceRepository;
import com.canteen.repository.AdminRepository;
import com.canteen.repository.DishRepository;
import com.canteen.repository.OrderRepository;
import com.canteen.repository.ProductionTaskRepository;
import com.canteen.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderRepository orderRepository;
    private final DishRepository dishRepository;
    private final AdminPerformanceRepository performanceRepository;
    private final AdminRepository adminRepository;
    private final ProductionTaskRepository productionTaskRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM-dd");

    @Override
    @Transactional(readOnly = true)
    public Result<Map<String, Object>> getSummary(String viewMode) {
        LocalDate today = LocalDate.now();
        LocalDateTime start;
        LocalDateTime end = today.atTime(LocalTime.MAX);

        if ("month".equals(viewMode)) {
            start = today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        } else {
            start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        }

        // 总营收
        BigDecimal totalRevenue = orderRepository.sumRevenueByDateRange(start, end);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        // 每日营收明细
        List<Object[]> dailyRevenue = orderRepository.sumRevenueDailyByDateRange(start, end);

        // 构建成本映射表：从 Dish 实体中获取 costPrice
        Map<String, BigDecimal> dishCostMap = new HashMap<>();
        List<Dish> allDishes = dishRepository.findAll();
        for (Dish d : allDishes) {
            BigDecimal cost = d.getCostPrice();
            if (cost == null) {
                // 默认成本为售价的40%
                cost = d.getPrice().multiply(BigDecimal.valueOf(0.4)).setScale(2, RoundingMode.HALF_UP);
            }
            dishCostMap.put(d.getName(), cost);
        }

        // 每日菜品销售 → 计算每日成本
        List<Object[]> dishSales = orderRepository.countSalesByDish(start, end);

        // 总成本 = SUM(每个菜品销量 × 成本单价)
        BigDecimal totalCost = BigDecimal.ZERO;
        for (Object[] row : dishSales) {
            String dishName = (String) row[0];
            long qty = ((Number) row[1]).longValue();
            BigDecimal unitCost = dishCostMap.getOrDefault(dishName, BigDecimal.valueOf(8));
            totalCost = totalCost.add(unitCost.multiply(BigDecimal.valueOf(qty)));
        }
        BigDecimal totalProfit = totalRevenue.subtract(totalCost);

        // 趋势数据：按日补0
        List<Map<String, Object>> trendData = new ArrayList<>();
        Map<String, BigDecimal> revenueByDate = new LinkedHashMap<>();
        for (Object[] row : dailyRevenue) {
            String dateStr;
            if (row[0] instanceof java.sql.Date) {
                dateStr = ((java.sql.Date) row[0]).toLocalDate().format(DATE_FMT);
            } else {
                dateStr = row[0].toString();
                if (dateStr.length() > 5) {
                    dateStr = LocalDate.parse(dateStr).format(DATE_FMT);
                }
            }
            revenueByDate.put(dateStr, (BigDecimal) row[1]);
        }

        LocalDate d = start.toLocalDate();
        while (!d.isAfter(today)) {
            String dateStr = d.format(DATE_FMT);
            BigDecimal rev = revenueByDate.getOrDefault(dateStr, BigDecimal.ZERO);
            // 估算当日成本占比
            BigDecimal dayCost = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? totalCost.multiply(rev).divide(totalRevenue, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal dayProfit = rev.subtract(dayCost);

            Map<String, Object> point = new LinkedHashMap<>();
            point.put("name", dateStr);
            point.put("营收", rev);
            point.put("成本", dayCost);
            point.put("利润", dayProfit);
            trendData.add(point);
            d = d.plusDays(1);
        }

        // 产品销量占比（前10）
        List<Map<String, Object>> productSales = new ArrayList<>();
        int limit = Math.min(dishSales.size(), 10);
        for (int i = 0; i < limit; i++) {
            Object[] row = dishSales.get(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", row[0]);
            item.put("value", ((Number) row[1]).intValue());
            productSales.add(item);
        }

        // 最佳员工
        List<Object[]> perfSummary = performanceRepository.summarizeByDateRange(start, end);
        String bestEmployee = "暂无";
        if (!perfSummary.isEmpty()) {
            Object[] top = perfSummary.get(0);
            bestEmployee = top[1] != null ? top[1].toString() : "暂无";
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalRevenue", totalRevenue);
        result.put("totalProfit", totalProfit);
        result.put("totalCost", totalCost);
        result.put("bestEmployee", bestEmployee);
        result.put("trendData", trendData);
        result.put("productSales", productSales);
        result.put("totalOrders", orderRepository.countCompletedByDateRange(start, end));
        result.put("avgOrderAmount", totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? totalRevenue.divide(BigDecimal.valueOf(Math.max(1, orderRepository.countCompletedByDateRange(start, end))), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

        return Result.ok(result);
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Map<String, Object>> getEmployees(String viewMode, int page, int pageSize,
                                                     String keyword, String role, int minScore) {
        LocalDate today = LocalDate.now();
        LocalDateTime start;
        LocalDateTime end = today.atTime(LocalTime.MAX);

        if ("month".equals(viewMode)) {
            start = today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        } else {
            start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        }

        // 查询所有拥有 chef 角色的管理员
        List<Admin> chefs = adminRepository.findByRoleName("chef");

        // 按关键词过滤
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim().toLowerCase() : null;
        if (kw != null) {
            chefs = chefs.stream().filter(a -> {
                String name = a.getRealName() != null ? a.getRealName().toLowerCase() : "";
                String uname = a.getUsername() != null ? a.getUsername().toLowerCase() : "";
                return name.contains(kw) || uname.contains(kw);
            }).toList();
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Admin chef : chefs) {
            BigDecimal totalScore = performanceRepository.sumScoreByAdmin(chef.getId(), start, end);
            long taskCount = performanceRepository.countByAdminAndDateRange(chef.getId(), start, end);

            if (totalScore.intValue() < minScore) continue;

            Map<String, Object> emp = new LinkedHashMap<>();
            emp.put("name", chef.getRealName() != null ? chef.getRealName() : chef.getUsername());
            emp.put("role", "厨师");
            emp.put("tasks", taskCount);
            Double avgRating = productionTaskRepository.findAverageRatingByChefId(chef.getId());
            emp.put("rating", avgRating != null ? String.format("%.1f", avgRating) : "暂无");
            emp.put("score", totalScore.setScale(0, RoundingMode.HALF_UP).toString());
            list.add(emp);
        }

        // 按分数降序排序
        list.sort((a, b) -> Integer.compare(
                Integer.parseInt(b.get("score").toString()),
                Integer.parseInt(a.get("score").toString())));

        // 分页
        int total = list.size();
        int fromIdx = Math.min((page - 1) * pageSize, total);
        int toIdx = Math.min(fromIdx + pageSize, total);
        List<Map<String, Object>> pagedList = list.subList(fromIdx, toIdx);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", pagedList);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);

        return Result.ok(result);
    }
}
