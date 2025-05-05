package pro.akosarev.sandbox.entity;

import java.time.Duration;

public class RateLimitConfig {
    private int maxRequests;
    private int durationMin;
    private int blockDurationMin;

    // Геттеры и сеттеры
    public int getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public int getDurationMin() {
        return durationMin;
    }

    public Duration getDuration() {
        return Duration.ofMinutes(Math.max(1, durationMin)); // Минимум 1 минута
    }

    public Duration getBlockDuration() {
        return Duration.ofMinutes(Math.max(1, blockDurationMin));
    }

    public void setDurationMin(int durationMin) {
        this.durationMin = durationMin;
    }

    public int getBlockDurationMin() {
        return blockDurationMin;
    }

    public void setBlockDurationMin(int blockDurationMin) {
        this.blockDurationMin = blockDurationMin;
    }
}
