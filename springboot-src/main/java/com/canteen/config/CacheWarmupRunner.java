package com.canteen.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 启动时清除可能因序列化配置变更而损坏的缓存数据
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheWarmupRunner implements ApplicationRunner {

    private final RedisConnectionFactory connectionFactory;

    @Override
    public void run(ApplicationArguments args) {
        try (var connection = connectionFactory.getConnection()) {
            // 清除所有 Spring Cache 产生的缓存键（格式一般为 cacheName::key）
            String[] cachePatterns = {
                    "menuCategories::*",
                    "userPermissions::*",
                    "userInfo::*",
                    "orderDetail::*",
                    "dashboardOverview::*"
            };
            for (String pattern : cachePatterns) {
                Set<byte[]> keys = connection.commands().keys(pattern.getBytes());
                if (keys != null && !keys.isEmpty()) {
                    connection.commands().del(keys.toArray(new byte[0][]));
                    log.info("已清除缓存 [{}]: {} 个键", pattern, keys.size());
                }
            }
            log.info("启动缓存清理完成");
        } catch (Exception e) {
            log.warn("启动缓存清理失败（可忽略）: {}", e.getMessage());
        }
    }
}
