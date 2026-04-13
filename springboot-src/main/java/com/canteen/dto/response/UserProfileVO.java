package com.canteen.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UserProfileVO {
    private Long id;
    private String name;
    private String phone;
    private String avatar;
    private String email;
    private Boolean isVip;
    private Integer vipLevel;
    private BigDecimal totalSpent;
    private BigDecimal nextLevelSpend;
    private BigDecimal balance;
    private Integer points;
    private Integer couponsCount;
    private List<CouponVO> coupons;
    private List<AddressVO> addresses;
    private List<RecordVO> records;

    @Data
    public static class CouponVO {
        private Long id;
        private String title;
        private BigDecimal amount;
        private BigDecimal minAmount;
        private String status;
        private String expiry;
    }

    @Data
    public static class AddressVO {
        private Long id;
        private String name;
        private String phone;
        private String detail;
        private Boolean isDefault;
    }

    @Data
    public static class RecordVO {
        private Long id;
        private String title;
        private String createdAt;
        private Boolean isAdd;
        private BigDecimal amount;
    }
}
