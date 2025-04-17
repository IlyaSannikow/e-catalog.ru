package pro.akosarev.sandbox.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RateLimiter {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(RateLimiter.class);

    public RateLimiter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String key, int maxRequests, Duration duration) {
        String rateLimitKey = "rate_limit:" + key;
        Long current = redisTemplate.opsForValue().increment(rateLimitKey);

        if (current == null) {
            logger.error("Failed to increment rate limit counter for key: {}", key);
            return false;
        }

        if (current == 1) {
            redisTemplate.expire(rateLimitKey, duration);
        }

        return current <= maxRequests;
    }
}
