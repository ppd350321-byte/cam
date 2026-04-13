package com.canteen.service.impl;

import com.canteen.common.exception.BusinessException;
import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.common.result.ResultCode;
import com.canteen.dto.request.*;
import com.canteen.dto.response.LoginVO;
import com.canteen.dto.response.UserProfileVO;
import com.canteen.dto.response.UserVO;
import com.canteen.entity.BalanceRecord;
import com.canteen.entity.PointsRecord;
import com.canteen.entity.User;
import com.canteen.entity.UserAddress;
import com.canteen.entity.UserCoupon;
import com.canteen.entity.VipLevel;
import com.canteen.repository.*;
import com.canteen.security.JwtTokenProvider;
import com.canteen.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BalanceRecordRepository balanceRecordRepository;
    private final UserCouponRepository userCouponRepository;
    private final CouponTemplateRepository couponTemplateRepository;
    private final PointsRecordRepository pointsRecordRepository;
    private final OrderRepository orderRepository;
    private final UserAddressRepository userAddressRepository;
    private final VipLevelRepository vipLevelRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    @Transactional
    public LoginVO login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }

        if (!"active".equals(user.getStatus()) || Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), "customer");

        // 删除该用户旧的 token 缓存（确保同一用户只有一个有效 token）
        String userTokenKey = "jwt:user-token:customer:" + user.getId();
        Object oldToken = redisTemplate.opsForValue().get(userTokenKey);
        if (oldToken != null) {
            redisTemplate.delete("jwt:token:" + oldToken);
        }

        // 将新 token 缓存到 Redis，TTL 与 JWT 过期时间一致
        long expirationSeconds = jwtTokenProvider.getRemainingExpiration(token);
        redisTemplate.opsForValue().set("jwt:token:" + token, user.getId(), expirationSeconds, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(userTokenKey, token, expirationSeconds, TimeUnit.SECONDS);

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setExpiresAt(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        LoginVO.UserLoginVO userVO = new LoginVO.UserLoginVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setName(user.getRealName());
        userVO.setIsAdmin(false);
        userVO.setRoleCodes(List.of());
        userVO.setRoleIds(List.of());
        userVO.setPermissions(List.of());
        // Note: mobile users have no role/permission concept; these fields are for LoginVO compatibility
        vo.setUser(userVO);

        return vo;
    }

    @Override
    @Transactional
    public LoginVO register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRealName(request.getRealName() != null ? request.getRealName() : request.getUsername());

        userRepository.save(user);

        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername(request.getUsername());
        loginReq.setPassword(request.getPassword());
        return login(loginReq);
    }

    @Override
    public void logout(String token) {
        if (token != null && jwtTokenProvider.validateToken(token)) {
            long remaining = jwtTokenProvider.getRemainingExpiration(token);
            redisTemplate.opsForValue().set("jwt:blacklist:" + token, "1", remaining, TimeUnit.SECONDS);
            // 移除缓存的 token 及用户反向映射
            redisTemplate.delete("jwt:token:" + token);
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            redisTemplate.delete("jwt:user-token:customer:" + userId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Result<PageResult<UserVO>> listUsers(UserPageQuery query) {
        String keyword = "all".equals(query.getKeyword()) ? null : query.getKeyword();
        String status = "all".equals(query.getStatus()) ? null : query.getStatus();

        Page<User> page = userRepository.findByFilters(
                keyword, query.getDepartment(), query.getIsVip(), status, query.toPageable());

        List<UserVO> list = page.getContent().stream().map(this::toUserVO).toList();
        return PageResult.of(list, page.getTotalElements(), query.getPage(), query.getPageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public UserVO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户不存在"));
        return toUserVO(user);
    }

    @Override
    @Transactional
    public UserVO updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户不存在"));

        if (request.getRealName() != null) user.setRealName(request.getRealName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getDepartment() != null) user.setDepartment(request.getDepartment());
        if (request.getIsVip() != null) user.setIsVip(request.getIsVip());

        userRepository.save(user);
        return toUserVO(user);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户不存在"));
        user.setStatus(status);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void rechargeBalance(Long id, BigDecimal amount) {
        User user = userRepository.findByIdWithLock(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户不存在"));
        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);

        BalanceRecord record = new BalanceRecord();
        record.setUserId(id);
        record.setType("recharge");
        record.setAmount(amount);
        record.setBalanceAfter(user.getBalance());
        record.setRemark("管理员充值");
        balanceRecordRepository.save(record);
    }

    @Override
    @Transactional
    public void addPoints(Long id, int points) {
        userRepository.incrementPoints(id, points);

        User user = userRepository.findById(id).orElse(null);
        PointsRecord pr = new PointsRecord();
        pr.setUserId(id);
        pr.setType("earn_order");
        pr.setPoints(points);
        pr.setPointsAfter(user != null ? user.getPoints() : null);
        pr.setRemark("消费获得" + points + "积分");
        pointsRecordRepository.save(pr);
    }

    @Override
    @Transactional
    public void deductBalance(Long userId, BigDecimal amount, Long refId) {
        User user = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户不存在"));

        if (user.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "余额不足");
        }

        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);

        BalanceRecord record = new BalanceRecord();
        record.setUserId(userId);
        record.setType("consume");
        record.setAmount(amount.negate());
        record.setBalanceAfter(user.getBalance());
        record.setRefId(refId);
        balanceRecordRepository.save(record);
    }

    @Override
    @Transactional
    public void checkAndUpgradeVip(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        BigDecimal orderSpent = userRepository.sumActualAmountByUserId(userId);
        BigDecimal vipSpent = balanceRecordRepository.sumVipPurchaseByUserId(userId);
        BigDecimal totalSpent = orderSpent.add(vipSpent);
        int newLevel = resolveVipLevel(totalSpent);
        if (newLevel != user.getVipLevel()) {
            user.setVipLevel(newLevel);
            user.setIsVip(newLevel > 0);
            userRepository.save(user);
        }
    }

    @Override
    @Transactional
    public void recalculateAllVipLevels() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (Boolean.TRUE.equals(user.getIsDeleted())) continue;
            BigDecimal orderSpent = userRepository.sumActualAmountByUserId(user.getId());
            BigDecimal vipSpent = balanceRecordRepository.sumVipPurchaseByUserId(user.getId());
            BigDecimal totalSpent = orderSpent.add(vipSpent);
            int newLevel = resolveVipLevel(totalSpent);
            if (newLevel != (user.getVipLevel() != null ? user.getVipLevel() : 0)) {
                user.setVipLevel(newLevel);
                user.setIsVip(newLevel > 0);
                userRepository.save(user);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileVO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户不存在"));

        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatarUrl());
        vo.setEmail(user.getEmail());
        vo.setIsVip(user.getIsVip());
        vo.setVipLevel(user.getVipLevel());

        BigDecimal orderSpent = userRepository.sumActualAmountByUserId(userId);
        BigDecimal vipSpent = balanceRecordRepository.sumVipPurchaseByUserId(userId);
        BigDecimal totalSpent = orderSpent.add(vipSpent);
        vo.setTotalSpent(totalSpent);

        List<VipLevel> levels = vipLevelRepository.findAllByOrderByLevelAsc();
        int currentLevel = user.getVipLevel() != null ? user.getVipLevel() : 0;
        BigDecimal nextLevelSpend = null;
        for (VipLevel vl : levels) {
            if (vl.getLevel() > currentLevel) {
                nextLevelSpend = vl.getMinSpend().subtract(totalSpent);
                if (nextLevelSpend.compareTo(BigDecimal.ZERO) < 0) nextLevelSpend = BigDecimal.ZERO;
                break;
            }
        }
        vo.setNextLevelSpend(nextLevelSpend);

        vo.setBalance(user.getBalance());
        vo.setPoints(user.getPoints());

        List<UserCoupon> coupons = userCouponRepository.findByUserIdAndStatus(userId, "unused");
        vo.setCouponsCount(coupons.size());
        vo.setCoupons(coupons.stream().map(c -> {
            UserProfileVO.CouponVO cv = new UserProfileVO.CouponVO();
            cv.setId(c.getId());
            cv.setStatus(c.getStatus());
            cv.setExpiry(c.getExpiresAt() != null ? c.getExpiresAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null);
            couponTemplateRepository.findById(c.getTemplateId()).ifPresent(tpl -> {
                cv.setTitle(tpl.getTitle());
                cv.setAmount(tpl.getAmount());
                cv.setMinAmount(tpl.getMinAmount());
            });
            return cv;
        }).toList());

        vo.setAddresses(userAddressRepository.findByUserIdAndIsDeletedFalseOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream().map(addr -> {
                    UserProfileVO.AddressVO av = new UserProfileVO.AddressVO();
                    av.setId(addr.getId());
                    av.setName(addr.getName());
                    av.setPhone(addr.getPhone());
                    av.setDetail(addr.getDetail());
                    av.setIsDefault(addr.getIsDefault());
                    return av;
                }).toList());

        List<BalanceRecord> balanceRecords = balanceRecordRepository.findByUserIdOrderByCreatedAtDesc(userId);
        vo.setRecords(balanceRecords.stream().map(r -> {
            UserProfileVO.RecordVO rv = new UserProfileVO.RecordVO();
            rv.setId(r.getId());
            rv.setTitle(resolveRecordTitle(r.getType()));
            rv.setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt().format(DATETIME_FMT) : null);
            rv.setIsAdd(r.getAmount() != null && r.getAmount().compareTo(java.math.BigDecimal.ZERO) > 0);
            rv.setAmount(r.getAmount() != null ? r.getAmount().abs() : java.math.BigDecimal.ZERO);
            return rv;
        }).toList());

        return vo;
    }

    private int resolveVipLevel(BigDecimal totalSpent) {
        List<VipLevel> levels = vipLevelRepository.findAllByOrderByLevelAsc();
        int result = 0;
        for (VipLevel vl : levels) {
            if (totalSpent.compareTo(vl.getMinSpend()) >= 0) {
                result = vl.getLevel();
            } else {
                break;
            }
        }
        return result;
    }

    private String resolveRecordTitle(String type) {
        if (type == null) return "其他";
        return switch (type) {
            case "recharge" -> "余额充值";
            case "consume" -> "余额扣减";
            case "wechat_pay" -> "微信支付";
            case "alipay_pay" -> "支付宝支付";
            case "refund" -> "退款";
            case "points_redeem" -> "积分兑换";
            case "vip_purchase" -> "购买VIP会员";
            default -> "其他";
        };
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setDepartment(user.getDepartment());
        vo.setIsVip(user.getIsVip());
        vo.setUserTypeLabel(Boolean.TRUE.equals(user.getIsVip()) ? "VIP" : "普通");
        vo.setBalance(user.getBalance());
        vo.setPoints(user.getPoints());
        vo.setLastVisitAt(user.getLastLoginAt() != null ? user.getLastLoginAt().format(DATETIME_FMT) : null);
        vo.setStatus(user.getStatus());
        return vo;
    }
}
