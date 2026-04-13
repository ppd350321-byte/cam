package com.canteen.controller;

import com.canteen.common.result.Result;
import com.canteen.entity.CouponTemplate;
import com.canteen.repository.CouponTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponTemplateRepository couponTemplateRepository;

    @GetMapping("/center")
    public Result<List<Map<String, Object>>> getCouponCenter() {
        List<CouponTemplate> templates = couponTemplateRepository.findByIsActiveTrue();
        List<Map<String, Object>> result = templates.stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("title", t.getTitle() != null ? t.getTitle() : "");
            m.put("amount", t.getAmount());
            m.put("minAmount", t.getMinAmount());
            return m;
        }).toList();
        return Result.ok(result);
    }

    @GetMapping("/redeemable")
    public Result<List<Map<String, Object>>> getRedeemableCoupons() {
        List<CouponTemplate> templates = couponTemplateRepository.findByIsActiveTrueAndPointsCostNotNull();
        List<Map<String, Object>> result = templates.stream()
                .filter(t -> t.getPointsCost() != null && t.getPointsCost() > 0)
                .map(t -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", t.getId());
                    m.put("title", t.getTitle() != null ? t.getTitle() : "");
                    m.put("amount", t.getAmount());
                    m.put("minAmount", t.getMinAmount());
                    m.put("pointsCost", t.getPointsCost());
                    m.put("validDays", t.getValidDays());
                    return m;
                }).toList();
        return Result.ok(result);
    }
}
