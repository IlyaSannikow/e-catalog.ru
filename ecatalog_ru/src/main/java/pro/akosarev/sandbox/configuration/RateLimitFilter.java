package pro.akosarev.sandbox.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pro.akosarev.sandbox.entity.RateLimitConfig;
import pro.akosarev.sandbox.service.LoginAttemptService;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final RateLimiter rateLimiter;
    private final SecurityProperties securityProperties;
    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);

    public RateLimitFilter(RateLimiter rateLimiter, SecurityProperties securityProperties) {
        this.rateLimiter = rateLimiter;
        this.securityProperties = securityProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String ipAddress = getClientIp(request);
        String requestUri = request.getRequestURI();

        // Пропускаем статические ресурсы
        if (shouldSkipRateLimit(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Получаем конфигурацию лимитов
        RateLimitConfig limitConfig = determineLimitConfig(requestUri, request.getMethod());
        if (limitConfig == null) {
            logger.error("Rate limit configuration not found for {}", requestUri);
            filterChain.doFilter(request, response);
            return;
        }

        // Проверяем блокировку IP
        if (isBlocked(ipAddress)) {
            long remainingTime = getRemainingBlockTime(ipAddress, TimeUnit.MINUTES);
            sendTooManyRequestsResponse(response,
                    "Too many requests. Try again in " + remainingTime + " minutes.");
            return;
        }

        // Проверяем лимит запросов
        String rateLimitKey = ipAddress + ":" + requestUri;
        if (!checkRequestLimit(rateLimitKey, limitConfig)) {
            blockClient(ipAddress, limitConfig.getBlockDuration());
            sendTooManyRequestsResponse(response,
                    "Too many requests. You have been blocked for " +
                            limitConfig.getBlockDuration().toMinutes() + " minutes.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isBlocked(String ipAddress) {
        try {
            return rateLimiter.isBlocked(ipAddress);
        } catch (Exception e) {
            logger.error("Failed to check block status for IP: {}", ipAddress, e);
            return false;
        }
    }

    private long getRemainingBlockTime(String ipAddress, TimeUnit timeUnit) {
        try {
            return rateLimiter.getRemainingBlockTime(ipAddress, timeUnit);
        } catch (Exception e) {
            logger.error("Failed to get remaining block time for IP: {}", ipAddress, e);
            return 0;
        }
    }

    private boolean checkRequestLimit(String rateLimitKey, RateLimitConfig limitConfig) {
        try {
            return rateLimiter.isAllowed(
                    rateLimitKey,
                    limitConfig.getMaxRequests(),
                    limitConfig.getDuration()
            );
        } catch (Exception e) {
            logger.error("Failed to check rate limit for key: {}", rateLimitKey, e);
            return true; // В случае ошибки пропускаем запрос
        }
    }

    private void blockClient(String ipAddress, Duration duration) {
        try {
            rateLimiter.blockKey(ipAddress, duration);
        } catch (Exception e) {
            logger.error("Failed to block IP: {}", ipAddress, e);
        }
    }

    private void sendTooManyRequestsResponse(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(message);
    }

    private RateLimitConfig determineLimitConfig(String requestUri, String method) {
        if (securityProperties == null || securityProperties.getRateLimit() == null) {
            logger.warn("Security properties or rate limit configuration not initialized");
            return getDefaultConfig();
        }

        // Проверяем специфичные endpoints
        if (requestUri.equals("/register") && "POST".equals(method)) {
            return securityProperties.getRateLimit().getEndpoints().getRegister();
        }
        if (requestUri.equals("/upload") && "POST".equals(method)) {
            return securityProperties.getRateLimit().getEndpoints().getUpload();
        }

        // Определяем категорию endpoint
        if (isPublicEndpoint(requestUri)) {
            return securityProperties.getRateLimit().getPublic();
        }

        // Все остальные аутентифицированные endpoints
        return securityProperties.getRateLimit().getProtected();
    }

    private boolean isPublicEndpoint(String requestUri) {
        return requestUri.equals("/login") ||
                requestUri.equals("/registration") ||
                requestUri.equals("/comic/share") ||
                requestUri.equals("/");
    }

    private RateLimitConfig getDefaultConfig() {
        RateLimitConfig config = new RateLimitConfig();
        config.setMaxRequests(100);
        config.setDurationMin(1);
        config.setBlockDurationMin(5);
        return config;
    }

    private boolean shouldSkipRateLimit(String requestUri) {
        return requestUri.startsWith("/public/") ||
                requestUri.startsWith("/js/") ||
                requestUri.startsWith("/resources/") ||
                requestUri.startsWith("/error") ||
                requestUri.equals("/csrf-token");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // Для случая, когда X-Forwarded-For содержит несколько IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
