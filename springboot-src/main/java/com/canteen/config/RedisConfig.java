package com.canteen.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig implements CachingConfigurer {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RedisConfig.class);

    /**
     * Redis 缓存异常时优雅降级：仅记录日志，不影响业务
     */
    @Override
    @Bean
    public org.springframework.cache.interceptor.CacheErrorHandler errorHandler() {
        return new org.springframework.cache.interceptor.SimpleCacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception,
                    org.springframework.cache.Cache cache, Object key) {
                log.warn("Cache GET failed [{}:{}]: {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception,
                    org.springframework.cache.Cache cache, Object key, Object value) {
                log.warn("Cache PUT failed [{}:{}]: {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception,
                    org.springframework.cache.Cache cache, Object key) {
                log.warn("Cache EVICT failed [{}:{}]: {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception,
                    org.springframework.cache.Cache cache) {
                log.warn("Cache CLEAR failed [{}]: {}", cache.getName(), exception.getMessage());
            }
        };
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.registerModule(new JavaTimeModule());

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 使用无参构造：内置的 default typing 能正确处理类型信息的序列化/反序列化
        GenericJackson2JsonRedisSerializer cacheSerializer = new GenericJackson2JsonRedisSerializer();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(cacheSerializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("menuCategories",
                        defaultConfig.entryTtl(Duration.ofMinutes(60)))
                .withCacheConfiguration("userPermissions",
                        defaultConfig.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration("userInfo",
                        defaultConfig.entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration("orderDetail",
                        defaultConfig.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration("dashboardOverview",
                        defaultConfig.entryTtl(Duration.ofMinutes(30)))
                .build();
    }
}
