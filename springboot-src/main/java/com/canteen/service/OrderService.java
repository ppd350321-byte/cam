package com.canteen.service;

import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.dto.request.CreateOrderRequest;
import com.canteen.dto.request.OrderPageQuery;
import com.canteen.dto.response.OrderVO;

import java.util.Map;

public interface OrderService {

    Result<PageResult<OrderVO>> listOrders(OrderPageQuery query);

    OrderVO getOrderById(Long id);

    OrderVO placeOrder(Long userId, CreateOrderRequest request);

    void acceptOrder(Long orderId, Long chefId);

    void markPrepared(Long orderId);

    void confirmPickup(Long orderId);

    void startDelivery(Long orderId);

    void completeOrder(Long orderId);

    void cancelOrder(Long orderId);

    void approveCancelOrder(Long orderId);

    void rejectCancelOrder(Long orderId);

    void markPaid(Long orderId);

    void payOrder(Long orderId, String paymentMethod, Long couponId);

    Result<PageResult<OrderVO>> listUserOrders(Long userId, int page, int pageSize);

    void submitReview(Long orderId, int rating, String comment);

    Map<String, Object> getReview(Long orderId);
}
