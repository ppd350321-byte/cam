package com.canteen.controller;

import com.canteen.common.result.Result;
import com.canteen.dto.response.UserProfileVO;
import com.canteen.entity.CouponTemplate;
import com.canteen.entity.PointsRecord;
import com.canteen.entity.User;
import com.canteen.entity.BalanceRecord;
import com.canteen.entity.UserAddress;
import com.canteen.entity.UserCoupon;
import com.canteen.repository.*;
import com.canteen.security.SecurityUser;
import com.canteen.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class MobileUserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserCouponRepository userCouponRepository;
    private final CouponTemplateRepository couponTemplateRepository;
    private final UserAddressRepository userAddressRepository;
    private final BalanceRecordRepository balanceRecordRepository;
    private final PointsRecordRepository pointsRecordRepository;
    private final PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile(@AuthenticationPrincipal SecurityUser securityUser) {
        UserProfileVO profile = userService.getUserProfile(securityUser.getId());
        return Result.ok(Map.of("user", profile));
    }

    @PatchMapping("/profile")
    public Result<Void> updateProfile(@AuthenticationPrincipal SecurityUser securityUser,
                                      @RequestBody Map<String, Object> body) {
        User user = userRepository.findById(securityUser.getId()).orElse(null);
        if (user == null) return Result.ok();

        if (body.containsKey("realName")) user.setRealName((String) body.get("realName"));
        if (body.containsKey("phone")) {
            String phone = (String) body.get("phone");
            if (phone != null && !phone.matches("^1[3-9]\\d{9}$")) {
                return Result.fail("手机号格式不正确");
            }
            user.setPhone(phone);
        }
        if (body.containsKey("email")) user.setEmail((String) body.get("email"));
        userRepository.save(user);
        return Result.ok();
    }

    @PostMapping("/password")
    public Result<Void> updatePassword(@AuthenticationPrincipal SecurityUser securityUser,
                                       @RequestBody Map<String, String> body) {
        User user = userRepository.findById(securityUser.getId()).orElse(null);
        if (user == null) return Result.fail("用户不存在");

        String oldPwd = body.get("oldPassword");
        if (oldPwd != null && !oldPwd.isEmpty()) {
            if (!passwordEncoder.matches(oldPwd, user.getPasswordHash())) {
                return Result.fail("原密码错误");
            }
        }
        String newPwd = body.get("newPassword");
        if (newPwd == null || newPwd.isEmpty()) return Result.fail("新密码不能为空");
        user.setPasswordHash(passwordEncoder.encode(newPwd));
        userRepository.save(user);
        return Result.ok();
    }

    @PostMapping("/avatar")
    public Result<Map<String, String>> updateAvatar(@AuthenticationPrincipal SecurityUser securityUser,
                                                     @RequestBody Map<String, String> body) {
        User user = userRepository.findById(securityUser.getId()).orElse(null);
        if (user == null) return Result.fail("用户不存在");
        String avatarUrl = body.getOrDefault("avatarUrl", body.get("avatar"));
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        return Result.ok(Map.of("avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : ""));
    }

    @PostMapping("/recharge")
    public Result<Void> recharge(@AuthenticationPrincipal SecurityUser securityUser,
                                 @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        userService.rechargeBalance(securityUser.getId(), amount);
        return Result.ok();
    }

    @PostMapping("/vip/purchase")
    public Result<Void> purchaseVip(@AuthenticationPrincipal SecurityUser securityUser,
                                    @RequestBody Map<String, Object> body) {
        User user = userRepository.findById(securityUser.getId()).orElse(null);
        if (user == null) return Result.fail("用户不存在");
        BigDecimal price = body.get("price") != null ? new BigDecimal(body.get("price").toString()) : BigDecimal.ZERO;

        // planId 决定月数：1→1月, 3→3月, 12→12月
        int planId = body.get("planId") != null ? Integer.parseInt(body.get("planId").toString()) : 1;
        int months = planId;

        // 余额支付时扣减余额
        String payMethod = body.get("payMethod") != null ? body.get("payMethod").toString() : "balance";
        if ("balance".equals(payMethod)) {
            if (user.getBalance().compareTo(price) < 0) {
                return Result.fail("余额不足");
            }
            user.setBalance(user.getBalance().subtract(price));
        }

        user.setIsVip(true);

        // 有效期叠加：若当前VIP未过期则在现有到期日上累加，否则从当前时间开始
        LocalDateTime baseTime = (user.getVipExpiresAt() != null && user.getVipExpiresAt().isAfter(LocalDateTime.now()))
                ? user.getVipExpiresAt()
                : LocalDateTime.now();
        user.setVipExpiresAt(baseTime.plusMonths(months));
        userRepository.save(user);

        if (price.compareTo(BigDecimal.ZERO) > 0) {
            BalanceRecord record = new BalanceRecord();
            record.setUserId(user.getId());
            record.setType("vip_purchase");
            record.setAmount(price.negate());
            record.setRemark("购买VIP会员(" + months + "个月)");
            balanceRecordRepository.save(record);
        }

        // 根据累计消费重新计算VIP等级
        userService.checkAndUpgradeVip(user.getId());

        return Result.ok();
    }

    @PostMapping("/points/redeem")
    public Result<Void> redeemPoints(@AuthenticationPrincipal SecurityUser securityUser,
                                     @RequestBody Map<String, Object> body) {
        Long templateId = body.get("templateId") != null ? Long.parseLong(body.get("templateId").toString()) : null;
        if (templateId == null) return Result.fail("请选择要兑换的优惠券");

        CouponTemplate tpl = couponTemplateRepository.findById(templateId).orElse(null);
        if (tpl == null || !Boolean.TRUE.equals(tpl.getIsActive())) return Result.fail("优惠券不存在或已下架");
        if (tpl.getPointsCost() == null || tpl.getPointsCost() <= 0) return Result.fail("该优惠券不支持积分兑换");

        int points = tpl.getPointsCost();
        User user = userRepository.findById(securityUser.getId()).orElse(null);
        if (user == null) return Result.fail("用户不存在");
        if (user.getPoints() < points) return Result.fail("积分不足");

        user.setPoints(user.getPoints() - points);
        userRepository.save(user);

        // 创建用户优惠券
        UserCoupon uc = new UserCoupon();
        uc.setUserId(user.getId());
        uc.setTemplateId(templateId);
        uc.setStatus("unused");
        uc.setExpiresAt(tpl.getExpiresAt() != null ? tpl.getExpiresAt()
                : LocalDateTime.now().plusDays(tpl.getValidDays() != null ? tpl.getValidDays() : 30));
        userCouponRepository.save(uc);

        BalanceRecord record = new BalanceRecord();
        record.setUserId(user.getId());
        record.setType("points_redeem");
        record.setAmount(BigDecimal.ZERO);
        record.setRemark("积分兑换优惠券「" + tpl.getTitle() + "」，消耗" + points + "积分");
        balanceRecordRepository.save(record);

        PointsRecord pr = new PointsRecord();
        pr.setUserId(user.getId());
        pr.setType("redeem_coupon");
        pr.setPoints(-points);
        pr.setPointsAfter(user.getPoints());
        pr.setRemark("兑换优惠券「" + tpl.getTitle() + "」");
        pointsRecordRepository.save(pr);

        return Result.ok();
    }

    @GetMapping("/coupons/available")
    public Result<List<Map<String, Object>>> getAvailableCoupons(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam(required = false) BigDecimal orderAmount) {
        List<UserCoupon> coupons = userCouponRepository.findByUserIdAndStatus(securityUser.getId(), "unused");
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserCoupon uc : coupons) {
            if (uc.getExpiresAt() != null && uc.getExpiresAt().isBefore(now)) continue;
            CouponTemplate tpl = couponTemplateRepository.findById(uc.getTemplateId()).orElse(null);
            if (tpl == null) continue;
            boolean usable = orderAmount == null || tpl.getMinAmount() == null
                    || orderAmount.compareTo(tpl.getMinAmount()) >= 0;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", uc.getId());
            item.put("templateId", tpl.getId());
            item.put("title", tpl.getTitle());
            item.put("amount", tpl.getAmount());
            item.put("minAmount", tpl.getMinAmount());
            item.put("usable", usable);
            item.put("expiry", uc.getExpiresAt() != null ? uc.getExpiresAt().toLocalDate().toString() : null);
            result.add(item);
        }
        return Result.ok(result);
    }

    @PostMapping("/coupons/claim")
    public Result<Void> claimCoupon(@AuthenticationPrincipal SecurityUser securityUser,
                                    @RequestBody Map<String, Object> body) {
        Long templateId = Long.parseLong(body.get("templateId").toString());
        CouponTemplate tpl = couponTemplateRepository.findById(templateId).orElse(null);
        if (tpl == null) return Result.fail("优惠券不存在");
        if (tpl.getRemainingQty() != null && tpl.getRemainingQty() <= 0) return Result.fail("优惠券已领完");

        UserCoupon uc = new UserCoupon();
        uc.setUserId(securityUser.getId());
        uc.setTemplateId(templateId);
        uc.setStatus("unused");
        uc.setExpiresAt(tpl.getExpiresAt() != null ? tpl.getExpiresAt()
                : LocalDateTime.now().plusDays(tpl.getValidDays() != null ? tpl.getValidDays() : 30));
        userCouponRepository.save(uc);

        if (tpl.getRemainingQty() != null) {
            tpl.setRemainingQty(tpl.getRemainingQty() - 1);
            couponTemplateRepository.save(tpl);
        }
        return Result.ok();
    }

    @PostMapping("/addresses")
    public Result<Void> upsertAddress(@AuthenticationPrincipal SecurityUser securityUser,
                                      @RequestBody Map<String, Object> body) {
        Long userId = securityUser.getId();
        Object rawId = body.get("id");
        Long addressId = (rawId != null && !rawId.toString().isBlank()) ? Long.parseLong(rawId.toString()) : null;

        UserAddress address;
        if (addressId != null) {
            address = userAddressRepository.findById(addressId).orElse(null);
            if (address == null || !address.getUserId().equals(userId)) {
                return Result.fail("地址不存在");
            }
        } else {
            address = new UserAddress();
            address.setUserId(userId);
        }

        if (body.containsKey("name")) address.setName((String) body.get("name"));
        if (body.containsKey("phone")) {
            String phone = (String) body.get("phone");
            if (phone != null && !phone.matches("^1[3-9]\\d{9}$")) {
                return Result.fail("手机号格式不正确");
            }
            address.setPhone(phone);
        }
        if (body.containsKey("detail")) address.setDetail((String) body.get("detail"));
        if (body.containsKey("isDefault")) {
            boolean isDefault = Boolean.parseBoolean(body.get("isDefault").toString());
            address.setIsDefault(isDefault);
            if (isDefault) {
                // 将该用户其他地址取消默认
                userAddressRepository.findByUserIdAndIsDeletedFalseOrderByIsDefaultDescCreatedAtDesc(userId)
                        .stream()
                        .filter(a -> !a.getId().equals(address.getId()) && Boolean.TRUE.equals(a.getIsDefault()))
                        .forEach(a -> {
                            a.setIsDefault(false);
                            userAddressRepository.save(a);
                        });
            }
        }

        userAddressRepository.save(address);
        return Result.ok();
    }

    @DeleteMapping("/addresses/{addressId}")
    public Result<Void> removeAddress(@AuthenticationPrincipal SecurityUser securityUser,
                                      @PathVariable Long addressId) {
        UserAddress address = userAddressRepository.findById(addressId).orElse(null);
        if (address != null && address.getUserId().equals(securityUser.getId())) {
            address.setIsDeleted(true);
            userAddressRepository.save(address);
        }
        return Result.ok();
    }

    // ---- 流水明细 ----

    @GetMapping("/records/cash")
    public Result<List<Map<String, Object>>> getCashRecords(@AuthenticationPrincipal SecurityUser securityUser) {
        List<BalanceRecord> records = balanceRecordRepository.findByUserIdOrderByCreatedAtDesc(securityUser.getId());
        List<Map<String, Object>> result = records.stream()
                .filter(r -> !"points_redeem".equals(r.getType()))
                .map(r -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", r.getId());
                    m.put("title", resolveCashTitle(r.getType()));
                    m.put("remark", r.getRemark());
                    m.put("amount", r.getAmount());
                    m.put("isAdd", r.getAmount() != null && r.getAmount().compareTo(BigDecimal.ZERO) > 0);
                    m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().format(DATETIME_FMT) : null);
                    return m;
                }).toList();
        return Result.ok(result);
    }

    @GetMapping("/records/points")
    public Result<List<Map<String, Object>>> getPointsRecords(@AuthenticationPrincipal SecurityUser securityUser) {
        List<PointsRecord> records = pointsRecordRepository.findByUserIdOrderByCreatedAtDesc(securityUser.getId());
        List<Map<String, Object>> result = records.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("title", resolvePointsTitle(r.getType()));
            m.put("remark", r.getRemark());
            m.put("points", r.getPoints());
            m.put("isAdd", r.getPoints() != null && r.getPoints() > 0);
            m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().format(DATETIME_FMT) : null);
            return m;
        }).toList();
        return Result.ok(result);
    }

    private String resolveCashTitle(String type) {
        if (type == null) return "其他";
        return switch (type) {
            case "recharge" -> "余额充值";
            case "consume" -> "余额支付";
            case "wechat_pay" -> "微信支付";
            case "alipay_pay" -> "支付宝支付";
            case "refund" -> "订单退款";
            case "vip_purchase" -> "购买VIP会员";
            default -> "其他";
        };
    }

    private String resolvePointsTitle(String type) {
        if (type == null) return "其他";
        return switch (type) {
            case "earn_order" -> "消费获得积分";
            case "earn_daily" -> "每日签到积分";
            case "redeem_coupon" -> "兑换优惠券";
            default -> "其他";
        };
    }
}
