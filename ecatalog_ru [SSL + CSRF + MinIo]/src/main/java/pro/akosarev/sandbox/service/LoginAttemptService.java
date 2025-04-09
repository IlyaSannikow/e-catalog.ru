package pro.akosarev.sandbox.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class LoginAttemptService {
    private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    private final Map<String, Long> blockedUsers = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 3;
    private static final long BLOCK_DURATION = 60 * 1000; // 1 minute in milliseconds

    public void loginFailed(String key) {
        if (key == null) return;

        if (isBlocked(key)) {
            return;
        }

        int attempts = attemptsCache.getOrDefault(key, 0) + 1;
        attemptsCache.put(key, attempts);

        if (attempts >= MAX_ATTEMPTS) {
            blockedUsers.put(key, System.currentTimeMillis());
            attemptsCache.remove(key);
        }
    }

    public void loginSucceeded(String key) {
        if (key == null) return;
        attemptsCache.remove(key);
        blockedUsers.remove(key);
    }

    public boolean isBlocked(String key) {
        if (key == null) return false;

        Long blockedTime = blockedUsers.get(key);
        if (blockedTime == null) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long blockTimeElapsed = currentTime - blockedTime;

        if (blockTimeElapsed >= BLOCK_DURATION) {
            blockedUsers.remove(key);
            return false;
        }

        return true;
    }
}
