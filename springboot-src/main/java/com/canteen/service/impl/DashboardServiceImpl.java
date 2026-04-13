package com.canteen.service.impl;

import com.canteen.dto.response.DashboardOverviewVO;
import com.canteen.entity.enums.OrderStatus;
import com.canteen.entity.enums.TaskStatus;
import com.canteen.repository.OrderRepository;
import com.canteen.repository.ProductionTaskRepository;
import com.canteen.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final ProductionTaskRepository productionTaskRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardOverviewVO getOverview() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        DashboardOverviewVO vo = new DashboardOverviewVO();

        BigDecimal todayRevenue = orderRepository.sumRevenueByDateRange(todayStart, todayEnd);
        vo.setTodayRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO);

        Long todayOrders = orderRepository.countCompletedByDateRange(todayStart, todayEnd);
        Long pendingOrders = orderRepository.countByOrderStatus(OrderStatus.PENDING_ACCEPT);
        Long productionTasks = productionTaskRepository.countByStatus(TaskStatus.IN_PROGRESS);

        vo.setTodayOrders(todayOrders);
        vo.setPendingOrders(pendingOrders);
        vo.setProductionTasks(productionTasks);

        // Build stats array for admin frontend
        List<DashboardOverviewVO.StatItem> stats = new ArrayList<>();
        DashboardOverviewVO.StatItem revenueStat = new DashboardOverviewVO.StatItem();
        revenueStat.setTitle("今日营收");
        revenueStat.setValue("¥ " + vo.getTodayRevenue().toPlainString());
        revenueStat.setTrend("+0%");
        revenueStat.setPositive(true);
        stats.add(revenueStat);

        DashboardOverviewVO.StatItem orderStat = new DashboardOverviewVO.StatItem();
        orderStat.setTitle("今日订单");
        orderStat.setValue(String.valueOf(todayOrders != null ? todayOrders : 0));
        orderStat.setTrend("+0%");
        orderStat.setPositive(true);
        stats.add(orderStat);

        DashboardOverviewVO.StatItem pendingStat = new DashboardOverviewVO.StatItem();
        pendingStat.setTitle("待处理订单");
        pendingStat.setValue(String.valueOf(pendingOrders != null ? pendingOrders : 0));
        pendingStat.setTrend("");
        pendingStat.setPositive(false);
        stats.add(pendingStat);

        DashboardOverviewVO.StatItem taskStat = new DashboardOverviewVO.StatItem();
        taskStat.setTitle("生产任务");
        taskStat.setValue(String.valueOf(productionTasks != null ? productionTasks : 0));
        taskStat.setTrend("");
        taskStat.setPositive(true);
        stats.add(taskStat);

        vo.setStats(stats);

        // ── 实时营收趋势（近7天每日营收） ──
        LocalDateTime weekStart = LocalDate.now().minusDays(6).atStartOfDay();
        List<Object[]> dailyRevenue = orderRepository.sumRevenueDailyByDateRange(weekStart, todayEnd);
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MM-dd");
        List<DashboardOverviewVO.RevenueDataPoint> revenueList = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            String label = d.format(dateFmt);
            BigDecimal amount = BigDecimal.ZERO;
            for (Object[] row : dailyRevenue) {
                if (row[0] != null && row[0].toString().endsWith(d.toString())) {
                    amount = row[1] instanceof BigDecimal ? (BigDecimal) row[1] : new BigDecimal(row[1].toString());
                    break;
                }
            }
            DashboardOverviewVO.RevenueDataPoint rp = new DashboardOverviewVO.RevenueDataPoint();
            rp.setName(label);
            rp.setValue(amount);
            revenueList.add(rp);
        }
        vo.setRevenueData(revenueList);

        return vo;
    }
}
