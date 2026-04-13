package com.canteen.controller;

import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.dto.response.DashboardOverviewVO;
import com.canteen.dto.response.OrderVO;
import com.canteen.dto.request.OrderPageQuery;
import com.canteen.service.DashboardService;
import com.canteen.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final OrderService orderService;

    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('dashboard:view')")
    public Result<DashboardOverviewVO> getOverview() {
        return Result.ok(dashboardService.getOverview());
    }

    @GetMapping("/recent-orders")
    @PreAuthorize("hasAuthority('dashboard:view')")
    public Result<PageResult<OrderVO>> getRecentOrders(OrderPageQuery query) {
        if (query.getPageSize() == 0) {
            query.setPageSize(5);
        }
        return orderService.listOrders(query);
    }
}
