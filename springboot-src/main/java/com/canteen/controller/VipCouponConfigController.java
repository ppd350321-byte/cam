package com.canteen.controller;

import com.canteen.common.result.Result;
import com.canteen.entity.CouponTemplate;
import com.canteen.entity.VipLevel;
import com.canteen.repository.CouponTemplateRepository;
import com.canteen.repository.VipLevelRepository;
import com.canteen.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/vip-coupon")
@RequiredArgsConstructor
public class VipCouponConfigController {

    private final VipLevelRepository vipLevelRepository;
    private final CouponTemplateRepository couponTemplateRepository;
    private final UserService userService;

    // ── VIP Level CRUD ──

    @GetMapping("/vip-levels")
    @PreAuthorize("hasAuthority('vip-coupon:view')")
    public Result<List<VipLevel>> listVipLevels() {
        return Result.ok(vipLevelRepository.findAllByOrderByLevelAsc());
    }

    @PostMapping("/vip-levels")
    @PreAuthorize("hasAuthority('vip-coupon:edit')")
    public Result<VipLevel> addVipLevel(@RequestBody VipLevel vipLevel) {
        vipLevel.setId(null);
        List<VipLevel> existing = vipLevelRepository.findAllByOrderByLevelAsc();
        int expectedLevel = existing.isEmpty() ? 0 : existing.get(existing.size() - 1).getLevel() + 1;
        if (vipLevel.getLevel() == null || vipLevel.getLevel() != expectedLevel) {
            return Result.fail("新增等级必须为 " + expectedLevel);
        }
        if (vipLevel.getLevel() == 0 && (vipLevel.getMinSpend() == null || vipLevel.getMinSpend().compareTo(java.math.BigDecimal.ZERO) != 0)) {
            return Result.fail("0级(非会员)的消费金额必须为0");
        }
        VipLevel saved = vipLevelRepository.save(vipLevel);
        userService.recalculateAllVipLevels();
        return Result.ok(saved);
    }

    @PutMapping("/vip-levels/{id}")
    @PreAuthorize("hasAuthority('vip-coupon:edit')")
    public Result<VipLevel> updateVipLevel(@PathVariable Long id, @RequestBody VipLevel body) {
        VipLevel existing = vipLevelRepository.findById(id).orElse(null);
        if (existing == null) return Result.fail("VIP等级不存在");
        // 等级值不允许修改
        if (existing.getLevel() == 0 && body.getMinSpend() != null && body.getMinSpend().compareTo(java.math.BigDecimal.ZERO) != 0) {
            return Result.fail("0级(非会员)的消费金额必须为0");
        }
        if (body.getMinSpend() != null) existing.setMinSpend(body.getMinSpend());
        if (body.getDiscount() != null) existing.setDiscount(body.getDiscount());
        if (body.getDailyPoints() != null) existing.setDailyPoints(body.getDailyPoints());
        VipLevel saved = vipLevelRepository.save(existing);
        userService.recalculateAllVipLevels();
        return Result.ok(saved);
    }

    @DeleteMapping("/vip-levels/{id}")
    @PreAuthorize("hasAuthority('vip-coupon:edit')")
    public Result<Void> deleteVipLevel(@PathVariable Long id) {
        VipLevel target = vipLevelRepository.findById(id).orElse(null);
        if (target == null) return Result.fail("VIP等级不存在");
        List<VipLevel> all = vipLevelRepository.findAllByOrderByLevelAsc();
        VipLevel highest = all.get(all.size() - 1);
        if (!highest.getId().equals(id)) {
            return Result.fail("只能从最高等级开始删除");
        }
        vipLevelRepository.deleteById(id);
        userService.recalculateAllVipLevels();
        return Result.ok();
    }

    // ── Coupon Template CRUD ──

    @GetMapping("/coupons")
    @PreAuthorize("hasAuthority('vip-coupon:view')")
    public Result<Map<String, Object>> listCoupons(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<CouponTemplate> p = couponTemplateRepository.findAll(
                PageRequest.of(page - 1, pageSize, Sort.by("id").descending()));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", p.getContent());
        data.put("total", p.getTotalElements());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return Result.ok(data);
    }

    @PostMapping("/coupons")
    @PreAuthorize("hasAuthority('vip-coupon:edit')")
    public Result<CouponTemplate> addCoupon(@RequestBody CouponTemplate tpl) {
        tpl.setId(null);
        if (tpl.getRemainingQty() == null && tpl.getTotalQty() != null) {
            tpl.setRemainingQty(tpl.getTotalQty());
        }
        return Result.ok(couponTemplateRepository.save(tpl));
    }

    @PutMapping("/coupons/{id}")
    @PreAuthorize("hasAuthority('vip-coupon:edit')")
    public Result<CouponTemplate> updateCoupon(@PathVariable Long id, @RequestBody CouponTemplate body) {
        CouponTemplate existing = couponTemplateRepository.findById(id).orElse(null);
        if (existing == null) return Result.fail("优惠券模板不存在");
        if (body.getTitle() != null) existing.setTitle(body.getTitle());
        if (body.getAmount() != null) existing.setAmount(body.getAmount());
        if (body.getMinAmount() != null) existing.setMinAmount(body.getMinAmount());
        if (body.getTotalQty() != null) existing.setTotalQty(body.getTotalQty());
        if (body.getRemainingQty() != null) existing.setRemainingQty(body.getRemainingQty());
        if (body.getPointsCost() != null) existing.setPointsCost(body.getPointsCost());
        if (body.getValidDays() != null) existing.setValidDays(body.getValidDays());
        if (body.getExpiresAt() != null) existing.setExpiresAt(body.getExpiresAt());
        if (body.getIsActive() != null) existing.setIsActive(body.getIsActive());
        return Result.ok(couponTemplateRepository.save(existing));
    }

    @DeleteMapping("/coupons/{id}")
    @PreAuthorize("hasAuthority('vip-coupon:edit')")
    public Result<Void> deleteCoupon(@PathVariable Long id) {
        couponTemplateRepository.deleteById(id);
        return Result.ok();
    }
}
