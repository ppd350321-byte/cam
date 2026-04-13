package com.canteen.util;

import java.util.Map;

public final class OrderStatusUtil {

    private static final Map<String, String> STATUS_LABELS = Map.of(
            "pending_payment", "待支付",
            "pending_accept", "待接单",
            "preparing", "制作中",
            "pending_pickup", "待取餐",
            "delivering", "派送中",
            "completed", "已完成",
            "cancelled", "已取消",
            "pending_cancel", "取消审核中"
    );

    private static final Map<String, String> PAYMENT_LABELS = Map.of(
            "balance", "预存余额",
            "wechat", "微信支付",
            "alipay", "支付宝",
            "unpaid", "未支付"
    );

    private OrderStatusUtil() {
    }

    public static String getStatusLabel(String status) {
        return STATUS_LABELS.getOrDefault(status, status);
    }

    public static String getPaymentLabel(String method) {
        return PAYMENT_LABELS.getOrDefault(method, method);
    }
}
