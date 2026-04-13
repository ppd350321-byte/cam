package com.canteen.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardOverviewVO {
    private BigDecimal todayRevenue;
    private Long todayOrders;
    private Long pendingOrders;
    private Long productionTasks;
    private List<StatItem> stats;
    private List<RevenueDataPoint> revenueData;

    @Data
    public static class StatItem {
        private String title;
        private String value;
        private String trend;
        private Boolean positive;
    }

    @Data
    public static class RevenueDataPoint {
        private String name;
        private BigDecimal value;
    }

}
