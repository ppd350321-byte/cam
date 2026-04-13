package com.canteen.service.impl;

import com.canteen.common.exception.BusinessException;
import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.common.result.ResultCode;
import com.canteen.dto.request.CreateOrderRequest;
import com.canteen.dto.request.OrderPageQuery;
import com.canteen.dto.response.OrderItemVO;
import com.canteen.dto.response.OrderVO;
import com.canteen.entity.Dish;
import com.canteen.entity.Order;
import com.canteen.entity.OrderItem;
import com.canteen.entity.User;
import com.canteen.entity.Admin;
import com.canteen.entity.AdminPerformance;
import com.canteen.entity.BalanceRecord;
import com.canteen.entity.UserCoupon;
import com.canteen.entity.CouponTemplate;
import com.canteen.entity.enums.OrderStatus;
import com.canteen.entity.enums.PaymentMethod;
import com.canteen.entity.enums.TaskStatus;
import com.canteen.entity.ProductionTask;
import com.canteen.repository.AdminRepository;
import com.canteen.repository.AdminPerformanceRepository;
import com.canteen.repository.BalanceRecordRepository;
import com.canteen.repository.DishRepository;
import com.canteen.repository.OrderRepository;
import com.canteen.repository.ProductionTaskRepository;
import com.canteen.repository.UserRepository;
import com.canteen.repository.UserCouponRepository;
import com.canteen.repository.CouponTemplateRepository;
import com.canteen.service.OrderService;
import com.canteen.service.UserService;
import com.canteen.util.OrderStatusUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DishRepository dishRepository;
    private final BalanceRecordRepository balanceRecordRepository;
    private final AdminRepository adminRepository;
    private final ProductionTaskRepository productionTaskRepository;
    private final AdminPerformanceRepository adminPerformanceRepository;
    private final UserService userService;
    private final UserCouponRepository userCouponRepository;
    private final CouponTemplateRepository couponTemplateRepository;

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    @Transactional(readOnly = true)
    public Result<PageResult<OrderVO>> listOrders(OrderPageQuery query) {
        String keyword = "all".equals(query.getKeyword()) ? null : query.getKeyword();
        OrderStatus status = null;
        if (query.getOrderStatus() != null && !"all".equals(query.getOrderStatus())) {
            status = OrderStatus.fromValue(query.getOrderStatus());
        }
        String paymentMethodStr = "all".equals(query.getPaymentMethod()) ? null : query.getPaymentMethod();
        PaymentMethod paymentMethod = null;
        if (paymentMethodStr != null) {
            try {
                paymentMethod = PaymentMethod.fromValue(paymentMethodStr);
            } catch (IllegalArgumentException ignored) {
            }
        }

        Page<Order> page = orderRepository.findByFilters(keyword, status, paymentMethod, query.toPageable());
        List<OrderVO> list = page.getContent().stream().map(this::toOrderVO).toList();
        return PageResult.of(list, page.getTotalElements(), query.getPage(), query.getPageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderVO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "订单不存在"));
        return toOrderVO(order);
    }

    @Override
    @Transactional
    public OrderVO placeOrder(Long userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户不存在"));

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUser(user);
        order.setCustomerName(user.getRealName());
        order.setRemark(request.getRemark());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Dish dish = dishRepository.findByIdWithLock(itemReq.getDishId())
                    .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "菜品不存在"));

            if (!dish.getAvailable() || Boolean.TRUE.equals(dish.getIsDeleted())) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "菜品 " + dish.getName() + " 已下架");
            }

            if (dish.getStock() < itemReq.getQuantity()) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR,
                        "菜品 " + dish.getName() + " 库存不足（剩余" + dish.getStock() + "份）");
            }

            // 扣减库存
            dish.setStock(dish.getStock() - itemReq.getQuantity());
            dishRepository.save(dish);

            // 计算 VIP 价格
            BigDecimal vipPrice;
            if (Boolean.TRUE.equals(dish.getUseCustomVipPrice()) && dish.getVipPrice() != null) {
                vipPrice = dish.getVipPrice();
            } else {
                vipPrice = dish.getPrice().multiply(new BigDecimal("0.95")).setScale(2, RoundingMode.HALF_UP);
            }
            BigDecimal unitPrice = Boolean.TRUE.equals(user.getIsVip()) ? vipPrice : dish.getPrice();

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setDish(dish);
            item.setDishName(dish.getName());
            item.setUnitPrice(unitPrice);
            item.setQuantity(itemReq.getQuantity());
            item.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity())));

            order.getItems().add(item);
            totalAmount = totalAmount.add(item.getSubtotal());
        }

        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);

        // 应用优惠券
        BigDecimal actualAmount = totalAmount;
        if (request.getCouponId() != null) {
            UserCoupon uc = userCouponRepository.findById(request.getCouponId())
                    .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "优惠券不存在"));
            if (!uc.getUserId().equals(userId) || !"unused".equals(uc.getStatus())) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "优惠券不可用");
            }
            CouponTemplate tpl = couponTemplateRepository.findById(uc.getTemplateId())
                    .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "优惠券模板不存在"));
            if (tpl.getMinAmount() != null && totalAmount.compareTo(tpl.getMinAmount()) < 0) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "订单金额未达到优惠券使用门槛");
            }
            BigDecimal discount = tpl.getAmount() != null ? tpl.getAmount() : BigDecimal.ZERO;
            order.setDiscountAmount(discount);
            actualAmount = totalAmount.subtract(discount).max(BigDecimal.ZERO);
            order.setCouponId(uc.getId());
            uc.setStatus("used");
            uc.setUsedAt(LocalDateTime.now());
            userCouponRepository.save(uc);
        }
        order.setActualAmount(actualAmount);

        if (request.getPaymentMethod() != null) {
            PaymentMethod pm = PaymentMethod.fromValue(request.getPaymentMethod());
            order.setPaymentMethod(pm);

            if (pm == PaymentMethod.BALANCE) {
                userService.deductBalance(userId, actualAmount, null);
                order.setOrderStatus(OrderStatus.PENDING_ACCEPT);
                order.setPaymentStatus("paid");
            }
        } else {
            order.setPaymentMethod(PaymentMethod.UNPAID);
        }

        orderRepository.save(order);
        return toOrderVO(order);
    }

    @Override
    @Transactional
    public void acceptOrder(Long orderId, Long chefId) {
        Order order = getAndLock(orderId);
        assertStatus(order, OrderStatus.PENDING_ACCEPT);
        order.setOrderStatus(OrderStatus.PREPARING);
        order.setAcceptedAt(LocalDateTime.now());
        orderRepository.save(order);

        // 创建生产任务并关联订单
        Admin chef = adminRepository.findById(chefId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "指定厨师不存在"));

        String itemsSummary = order.getItems().stream()
                .map(item -> item.getDishName() + "×" + item.getQuantity())
                .collect(Collectors.joining(", "));

        ProductionTask task = new ProductionTask();
        task.setTaskNo("TASK-" + order.getOrderNo().replace("ORD-", ""));
        task.setOrder(order);
        task.setDishName(itemsSummary);
        task.setQuantity(order.getItems().stream().mapToInt(OrderItem::getQuantity).sum());
        task.setChef(chef);
        task.setChefName(chef.getRealName() != null ? chef.getRealName() : chef.getUsername());
        task.setPlannedStart(LocalDateTime.now());
        task.setPlannedEnd(LocalDateTime.now().plusMinutes(30));
        task.setStatus(TaskStatus.PENDING);
        task.setProgress(0);
        productionTaskRepository.save(task);
    }

    @Override
    @Transactional
    public void markPrepared(Long orderId) {
        Order order = getAndLock(orderId);
        assertStatus(order, OrderStatus.PREPARING);
        order.setOrderStatus(OrderStatus.PENDING_PICKUP);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void confirmPickup(Long orderId) {
        Order order = getAndLock(orderId);
        assertStatus(order, OrderStatus.DELIVERING);
        order.setOrderStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        orderRepository.save(order);

        // 完成关联的生产任务并记录绩效
        productionTaskRepository.findByOrderId(orderId).ifPresent(task -> {
            if (task.getStatus() != TaskStatus.COMPLETED) {
                task.setStatus(TaskStatus.COMPLETED);
                task.setProgress(100);
                task.setActualEnd(LocalDateTime.now());
                productionTaskRepository.save(task);
            }
            // 如果尚未记录绩效（按任务查找），则记录
            if (task.getChef() != null) {
                long existing = adminPerformanceRepository.countByAdminAndTask(task.getChef().getId(), task.getId());
                if (existing == 0) {
                    AdminPerformance perf = new AdminPerformance();
                    perf.setAdmin(task.getChef());
                    perf.setTask(task);
                    perf.setDishName(task.getDishName());
                    BigDecimal score;
                    if (task.getDish() != null && task.getDish().getPrice() != null) {
                        perf.setDishPrice(task.getDish().getPrice());
                        score = task.getDish().getPrice()
                                .multiply(BigDecimal.valueOf(task.getQuantity() != null ? task.getQuantity() : 1));
                    } else if (order.getActualAmount() != null) {
                        perf.setDishPrice(order.getActualAmount());
                        score = order.getActualAmount();
                    } else {
                        score = BigDecimal.valueOf(task.getQuantity() != null ? task.getQuantity() : 1);
                    }
                    perf.setScore(score);
                    perf.setRemark("完成任务 " + task.getTaskNo());
                    adminPerformanceRepository.save(perf);
                }
            }
        });

        int points = order.getActualAmount().intValue() * 10;
        userService.addPoints(order.getUser().getId(), points);
        userService.checkAndUpgradeVip(order.getUser().getId());
    }

    @Override
    @Transactional
    public void startDelivery(Long orderId) {
        Order order = getAndLock(orderId);
        assertStatus(order, OrderStatus.PENDING_PICKUP);
        order.setOrderStatus(OrderStatus.DELIVERING);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void completeOrder(Long orderId) {
        confirmPickup(orderId);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = getAndLock(orderId);
        if (order.getOrderStatus() == OrderStatus.COMPLETED ||
                order.getOrderStatus() == OrderStatus.CANCELLED ||
                order.getOrderStatus() == OrderStatus.PENDING_CANCEL ||
                order.getOrderStatus() == OrderStatus.DELIVERING) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该订单不可取消");
        }
        if (order.getOrderStatus() == OrderStatus.PENDING_PAYMENT) {
            // 未支付 → 直接取消
            order.setOrderStatus(OrderStatus.CANCELLED);
            order.setCancelledAt(LocalDateTime.now());
            restoreStock(order);
        } else {
            // 已支付 → 进入取消审核
            order.setOrderStatus(OrderStatus.PENDING_CANCEL);
        }
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void approveCancelOrder(Long orderId) {
        Order order = getAndLock(orderId);
        assertStatus(order, OrderStatus.PENDING_CANCEL);
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        orderRepository.save(order);
        restoreStock(order);
        // TODO: 退款逻辑
    }

    @Override
    @Transactional
    public void rejectCancelOrder(Long orderId) {
        Order order = getAndLock(orderId);
        assertStatus(order, OrderStatus.PENDING_CANCEL);
        // 驳回后恢复为待接单状态
        order.setOrderStatus(OrderStatus.PENDING_ACCEPT);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void markPaid(Long orderId) {
        Order order = getAndLock(orderId);
        assertStatus(order, OrderStatus.PENDING_PAYMENT);
        order.setOrderStatus(OrderStatus.PENDING_ACCEPT);
        order.setPaymentStatus("paid");
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void payOrder(Long orderId, String paymentMethod, Long couponId) {
        Order order = getAndLock(orderId);
        assertStatus(order, OrderStatus.PENDING_PAYMENT);

        // 应用优惠券
        if (couponId != null) {
            UserCoupon uc = userCouponRepository.findById(couponId)
                    .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "优惠券不存在"));
            if (!uc.getUserId().equals(order.getUser().getId()) || !"unused".equals(uc.getStatus())) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "优惠券不可用");
            }
            CouponTemplate tpl = couponTemplateRepository.findById(uc.getTemplateId())
                    .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "优惠券模板不存在"));
            if (tpl.getMinAmount() != null && order.getTotalAmount().compareTo(tpl.getMinAmount()) < 0) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "订单金额未达到优惠券使用门槛");
            }
            BigDecimal discount = tpl.getAmount() != null ? tpl.getAmount() : BigDecimal.ZERO;
            order.setDiscountAmount(discount);
            order.setActualAmount(order.getTotalAmount().subtract(discount).max(BigDecimal.ZERO));
            order.setCouponId(uc.getId());
            uc.setStatus("used");
            uc.setUsedAt(LocalDateTime.now());
            userCouponRepository.save(uc);
        }

        PaymentMethod pm = PaymentMethod.fromValue(paymentMethod);
        order.setPaymentMethod(pm);

        if (pm == PaymentMethod.BALANCE) {
            userService.deductBalance(order.getUser().getId(), order.getActualAmount(), orderId);
        } else {
            // 微信/支付宝支付 — 记录一条消费记录用于前端展示
            BalanceRecord record = new BalanceRecord();
            record.setUserId(order.getUser().getId());
            record.setType(pm == PaymentMethod.WECHAT ? "wechat_pay" : "alipay_pay");
            record.setAmount(order.getActualAmount().negate());
            record.setRefId(orderId);
            record.setRemark(pm.getValue() + "支付订单");
            balanceRecordRepository.save(record);
        }

        order.setOrderStatus(OrderStatus.PENDING_ACCEPT);
        order.setPaymentStatus("paid");
        orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Result<PageResult<OrderVO>> listUserOrders(Long userId, int page, int pageSize) {
        Page<Order> orderPage = orderRepository.findByUserId(userId, PageRequest.of(page - 1, pageSize));
        List<OrderVO> list = orderPage.getContent().stream().map(this::toOrderVO).toList();
        return PageResult.of(list, orderPage.getTotalElements(), page, pageSize);
    }

    private Order getAndLock(Long orderId) {
        return orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "订单不存在"));
    }

    private void assertStatus(Order order, OrderStatus required) {
        if (order.getOrderStatus() != required) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR,
                    "订单状态异常：当前=" + order.getOrderStatus().getValue() + "，期望=" + required.getValue());
        }
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            if (item.getDish() != null) {
                Dish dish = dishRepository.findByIdWithLock(item.getDish().getId()).orElse(null);
                if (dish != null) {
                    dish.setStock(dish.getStock() + item.getQuantity());
                    dishRepository.save(dish);
                }
            }
        }
    }

    private String generateOrderNo() {
        String datePart = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ORD-" + datePart + "-" + uuid;
    }

    private OrderVO toOrderVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setCustomerName(order.getCustomerName());
        vo.setTotalAmount(order.getActualAmount());

        String statusValue = order.getOrderStatus().getValue();
        vo.setOrderStatus(statusValue);
        vo.setOrderStatusLabel(OrderStatusUtil.getStatusLabel(statusValue));

        if (order.getPaymentMethod() != null) {
            String pmValue = order.getPaymentMethod().getValue();
            vo.setPaymentMethod(pmValue);
            vo.setPaymentMethodLabel(OrderStatusUtil.getPaymentLabel(pmValue));
        }

        vo.setCreatedAt(order.getCreatedAt() != null ? order.getCreatedAt().format(DATETIME_FMT) : null);
        vo.setPickupTime(order.getPickupTime());

        // displayTime: human-readable relative time for admin dashboard
        if (order.getCreatedAt() != null) {
            long minutes = Duration.between(order.getCreatedAt(), LocalDateTime.now()).toMinutes();
            if (minutes < 1) vo.setDisplayTime("刚刚");
            else if (minutes < 60) vo.setDisplayTime(minutes + "分钟前");
            else if (minutes < 1440) vo.setDisplayTime((minutes / 60) + "小时前");
            else vo.setDisplayTime((minutes / 1440) + "天前");
        }

        List<OrderItemVO> items = order.getItems().stream().map(item -> {
            OrderItemVO itemVO = new OrderItemVO();
            itemVO.setId(item.getId());
            itemVO.setName(item.getDishName());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setUnitPrice(item.getUnitPrice());
            itemVO.setPrice(item.getUnitPrice());
            if (item.getDish() != null) {
                itemVO.setDishId(item.getDish().getId());
                itemVO.setImage(item.getDish().getImageUrl());
            }
            return itemVO;
        }).toList();
        vo.setItems(items);

        vo.setItemsSummary(items.stream()
                .map(i -> i.getName() + "×" + i.getQuantity())
                .collect(Collectors.joining(", ")));

        return vo;
    }

    @Override
    @Transactional
    public void submitReview(Long orderId, int rating, String comment) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "订单不存在"));
        order.setRating(Math.max(1, Math.min(5, rating)));
        order.setReviewComment(comment);
        orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getReview(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "订单不存在"));
        if (order.getRating() == null) return null;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rating", order.getRating());
        result.put("comment", order.getReviewComment());
        return result;
    }
}
