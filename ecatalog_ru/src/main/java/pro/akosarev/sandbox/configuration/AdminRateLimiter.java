package pro.akosarev.sandbox.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class AdminRateLimiter {
    private final RedisTemplate<String, Object> adminRedisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(AdminRateLimiter.class);

    public AdminRateLimiter(@Qualifier("adminRedisTemplate") RedisTemplate<String, Object> adminRedisTemplate) {
        this.adminRedisTemplate = adminRedisTemplate;
    }

    public boolean isAllowed(String key) {
        return true; // Всегда разрешаем запросы администраторов
    }
}
