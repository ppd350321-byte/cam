package com.canteen.controller;

import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.dto.request.CreateOrderRequest;
import com.canteen.dto.request.OrderPageQuery;
import com.canteen.dto.response.OrderVO;
import com.canteen.entity.Admin;
import com.canteen.repository.AdminRepository;
import com.canteen.security.SecurityUser;
import com.canteen.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final AdminRepository adminRepository;

    /**
     * Admin: list all orders with filter; Mobile: list current user's orders (wrapped as {orders: [...]})
     */
    @GetMapping
    public Result<?> listOrders(@AuthenticationPrincipal SecurityUser user, OrderPageQuery query) {
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("orders:view"));
        if (isAdmin) {
            return orderService.listOrders(query);
        }
        Result<PageResult<OrderVO>> result = orderService.listUserOrders(user.getId(), query.getPage(), query.getPageSize());
        List<OrderVO> orders = result.getData() != null ? result.getData().getList() : List.of();
        return Result.ok(Map.of("orders", orders));
    }

    @PostMapping
    public Result<OrderVO> createOrder(@AuthenticationPrincipal SecurityUser user,
                                       @Valid @RequestBody CreateOrderRequest request) {
        return Result.ok(orderService.placeOrder(user.getId(), request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('orders:accept','orders:complete','orders:pickup')")
    public Result<Void> updateOrderStatus(@PathVariable Long id,
                                          @RequestBody Map<String, Object> body) {
        String status = body.containsKey("status") ? String.valueOf(body.get("status"))
                : body.containsKey("orderStatus") ? String.valueOf(body.get("orderStatus")) : null;
        if (status == null) return Result.fail("缺少状态参数");
        switch (status) {
            case "pending_accept", "preparing" -> {
                Long chefId = body.containsKey("chefId") ? Long.valueOf(String.valueOf(body.get("chefId"))) : null;
                if (chefId == null) return Result.fail("接单时必须指定厨师");
                orderService.acceptOrder(id, chefId);
            }
            case "pending_pickup" -> orderService.markPrepared(id);
            case "delivering" -> orderService.startDelivery(id);
            case "completed" -> orderService.completeOrder(id);
            case "cancelled" -> orderService.cancelOrder(id);
        }
        return Result.ok();
    }

    @GetMapping("/chefs")
    @PreAuthorize("hasAuthority('orders:accept')")
    public Result<List<Map<String, Object>>> listChefs() {
        List<Admin> chefs = adminRepository.findByRoleName("chef");
        List<Map<String, Object>> result = chefs.stream().map(a -> Map.<String, Object>of(
                "id", a.getId(),
                "name", a.getRealName() != null ? a.getRealName() : a.getUsername()
        )).toList();
        return Result.ok(result);
    }

    @PostMapping("/{id}/approve-cancel")
    @PreAuthorize("hasAnyAuthority('orders:accept','orders:complete')")
    public Result<Void> approveCancelOrder(@PathVariable Long id) {
        orderService.approveCancelOrder(id);
        return Result.ok();
    }

    @PostMapping("/{id}/reject-cancel")
    @PreAuthorize("hasAnyAuthority('orders:accept','orders:complete')")
    public Result<Void> rejectCancelOrder(@PathVariable Long id) {
        orderService.rejectCancelOrder(id);
        return Result.ok();
    }

    @PostMapping("/{id}/pay")
    public Result<Void> payOrder(@AuthenticationPrincipal SecurityUser user,
                                 @PathVariable Long id,
                                 @RequestBody Map<String, Object> body) {
        String paymentMethod = (String) body.getOrDefault("paymentMethod", "wechat");
        Long couponId = body.get("couponId") != null ? Long.valueOf(body.get("couponId").toString()) : null;
        orderService.payOrder(id, paymentMethod, couponId);
        return Result.ok();
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return Result.ok();
    }

    @PostMapping("/{id}/review")
    public Result<Void> submitReview(@PathVariable Long id,
                                     @RequestBody Map<String, Object> body) {
        int rating = body.get("rating") != null ? ((Number) body.get("rating")).intValue() : 5;
        String comment = body.get("comment") != null ? body.get("comment").toString() : "";
        orderService.submitReview(id, rating, comment);
        return Result.ok();
    }

    @GetMapping("/{id}/review")
    public Result<Map<String, Object>> getReview(@PathVariable Long id) {
        return Result.ok(orderService.getReview(id));
    }
}
