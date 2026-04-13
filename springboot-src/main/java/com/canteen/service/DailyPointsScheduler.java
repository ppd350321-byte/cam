package com.canteen.service;

import com.canteen.entity.User;
import com.canteen.entity.VipLevel;
import com.canteen.repository.UserRepository;
import com.canteen.repository.VipLevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class DailyPointsScheduler {

    private final UserRepository userRepository;
    private final VipLevelRepository vipLevelRepository;

    /**
     * 每天凌晨0点执行：根据 t_vip_level 配置的 dailyPoints 给用户发放积分
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void distributeDailyPoints() {
        List<VipLevel> levels = vipLevelRepository.findAllByOrderByLevelAsc();
        if (levels.isEmpty()) {
            log.info("未配置VIP等级，跳过每日积分发放");
            return;
        }

        Map<Integer, Integer> levelPointsMap = levels.stream()
                .filter(l -> l.getDailyPoints() != null && l.getDailyPoints() > 0)
                .collect(Collectors.toMap(VipLevel::getLevel, VipLevel::getDailyPoints));

        if (levelPointsMap.isEmpty()) {
            log.info("所有VIP等级的每天积分均为0，跳过发放");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<User> users = userRepository.findAll();
        int count = 0;

        for (User user : users) {
            if (Boolean.TRUE.equals(user.getIsDeleted()) || !"active".equals(user.getStatus())) {
                continue;
            }
            int userLevel = user.getVipLevel() != null ? user.getVipLevel() : 0;
            Integer dailyPoints = levelPointsMap.get(userLevel);
            if (dailyPoints != null && dailyPoints > 0) {
                // VIP用户需检查是否在有效期内
                if (userLevel > 0 && (user.getVipExpiresAt() == null || user.getVipExpiresAt().isBefore(now))) {
                    continue;
                }
                user.setPoints(user.getPoints() + dailyPoints);
                userRepository.save(user);
                count++;
            }
        }

        log.info("每日积分发放完成，共计发放给 {} 位用户", count);
    }
}
