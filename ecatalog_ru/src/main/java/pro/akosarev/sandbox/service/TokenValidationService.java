package pro.akosarev.sandbox.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import pro.akosarev.sandbox.repository.UserLogoutEventRepository;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public class TokenValidationService {
    private final JdbcTemplate jdbcTemplate;
    private final List<String> allowedOrigins;
    private final UserLogoutEventRepository userLogoutEventRepository;

    public TokenValidationService(JdbcTemplate jdbcTemplate,
                                  List<String> allowedOrigins,
                                  UserLogoutEventRepository userLogoutEventRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.allowedOrigins = allowedOrigins;
        this.userLogoutEventRepository = userLogoutEventRepository;
    }

    public boolean validateToken(String token, String userId, HttpServletRequest request) {
        System.out.println("Starting token validation for token: " + token + ", user: " + userId);

        // 1. Проверка origins
        if (!validateOrigin(request)) {
            System.out.println("Token validation failed: Invalid origin");
            return false;
        }
        System.out.println("Origin validation passed");

        // 2. Проверка времени жизни токена из БД
        if (!validateTokenExpiration(token)) {
            System.out.println("Token validation failed: Token expired or not found in DB");
            return false;
        }
        System.out.println("Token expiration validation passed");

        // 3. Проверка, что токен не в списке недействительных (logout)
        if (isTokenInvalidated(token, userId)) {
            System.out.println("Token validation failed: Token was invalidated (logout)");
            return false;
        }
        System.out.println("Token invalidation check passed");

        System.out.println("Token validation successful");
        return true;
    }

    private boolean validateOrigin(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");

        System.out.println("Validating origin. Origin header: " + origin + ", Referer: " + referer);

        // Разрешенные запросы без Origin/Referer (например, для мобильных приложений)
        if (origin == null && referer == null) {
            System.out.println("No Origin/Referer headers - allowing for mobile apps");
            return true;
        }

        // Проверка Origin
        if (origin != null) {
            try {
                // Нормализуем origin (удаляем trailing slash если есть)
                origin = origin.endsWith("/") ? origin.substring(0, origin.length() - 1) : origin;
                boolean originValid = allowedOrigins.contains(origin);
                System.out.println("Origin validation result: " + originValid);
                return originValid;
            } catch (Exception e) {
                System.out.println("Invalid Origin header format: " + origin);
                return false;
            }
        }

        // Проверка Referer
        if (referer != null) {
            try {
                // Удаляем path и query из Referer для проверки только домена
                URI refererUri = new URI(referer);
                String refererHost = refererUri.getHost();
                if (refererHost == null) {
                    System.out.println("Invalid Referer - no host: " + referer);
                    return false;
                }

                // Проверяем каждый allowed origin
                for (String allowedOrigin : allowedOrigins) {
                    try {
                        URI allowedUri = new URI(allowedOrigin);
                        if (refererHost.equalsIgnoreCase(allowedUri.getHost())) {
                            System.out.println("Referer validation passed for host: " + refererHost);
                            return true;
                        }
                    } catch (URISyntaxException e) {
                        System.out.println("Invalid allowed origin format: " + allowedOrigin);
                        continue;
                    }
                }
                System.out.println("Referer host not in allowed origins: " + refererHost);
                return false;
            } catch (URISyntaxException e) {
                System.out.println("Invalid Referer header format: " + referer);
                return false;
            }
        }

        System.out.println("No valid Origin/Referer found");
        return false;
    }

    private boolean validateTokenExpiration(String token) {
        try {
            System.out.println("Checking token expiration in DB for token: " + token);

            // Предполагаем, что у вас есть таблица active_tokens с полями token и expires_at
            String sql = "SELECT expires_at FROM active_tokens WHERE token = ?";
            Timestamp expiresAt = jdbcTemplate.queryForObject(sql,
                    new Object[]{token},
                    (rs, rowNum) -> rs.getTimestamp("expires_at"));

            if (expiresAt == null) {
                System.out.println("Token not found in DB");
                return false;
            }

            boolean isValid = expiresAt.toInstant().isAfter(Instant.now());
            System.out.println("Token expiration check result: " + isValid +
                    ", expires at: " + expiresAt);

            return isValid;
        } catch (EmptyResultDataAccessException e) {
            System.out.println("Token not found in DB");
            return false;
        } catch (Exception e) {
            System.out.println("Error checking token expiration: " + e.getMessage());
            return false;
        }
    }

    private boolean isTokenInvalidated(String token, String userId) {
        System.out.println("Checking if token was invalidated (logout)");
        // Проверяем, есть ли событие выхода для этого пользователя и токена
        return userLogoutEventRepository.existsByToken(token) ||
                userLogoutEventRepository.existsLogoutEventAfter(userId, new Date());
    }
}
