package pro.akosarev.sandbox.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimiter {
    private static final String BLOCK_PREFIX = "block:";
    private final RedisTemplate<String, Object> redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(RateLimiter.class);

    public RateLimiter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String key, int maxRequests, Duration duration) {
        try {
            long seconds = duration.getSeconds();
            if (seconds <= 0) {
                logger.warn("Invalid duration: {}", duration);
                return false;
            }

            Long current = redisTemplate.opsForValue().increment(key);
            if (current == null) {
                logger.error("Redis increment failed for key: {}", key);
                return false;
            }

            if (current == 1) {
                redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
            }

            return current <= maxRequests;
        } catch (Exception e) {
            logger.error("Rate limiting error", e);
            return false;
        }
    }

    public void blockKey(String key, Duration duration) {
        try {
            long seconds = duration.getSeconds();
            if (seconds <= 0) {
                logger.warn("Invalid block duration: {}", duration);
                return;
            }

            redisTemplate.opsForValue().set(
                    BLOCK_PREFIX + key,
                    "blocked",
                    seconds,
                    TimeUnit.SECONDS
            );
            logger.info("Blocked key: {} for {} seconds", key, seconds);
        } catch (Exception e) {
            logger.error("Failed to block key: {}", key, e);
        }
    }

    public boolean isBlocked(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(BLOCK_PREFIX + key);
            return exists != null && exists;
        } catch (Exception e) {
            logger.error("Failed to check block status for key: {}", key, e);
            return false;
        }
    }

    public long getRemainingBlockTime(String key, TimeUnit timeUnit) {
        try {
            Long expire = redisTemplate.getExpire(BLOCK_PREFIX + key, timeUnit);
            return expire != null ? expire : 0;
        } catch (Exception e) {
            logger.error("Failed to get remaining block time for key: {}", key, e);
            return 0;
        }
    }

    // Дополнительный метод для снятия блокировки
    public void unblockKey(String key) {
        try {
            redisTemplate.delete(BLOCK_PREFIX + key);
            logger.info("Unblocked key: {}", key);
        } catch (Exception e) {
            logger.error("Failed to unblock key: {}", key, e);
        }
    }
}