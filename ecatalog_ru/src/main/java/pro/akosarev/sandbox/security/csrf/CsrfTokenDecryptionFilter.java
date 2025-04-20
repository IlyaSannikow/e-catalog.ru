package pro.akosarev.sandbox.security.csrf;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class CsrfTokenDecryptionFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(CsrfTokenDecryptionFilter.class);

    private final JweCsrfTokenRepository csrfTokenRepository;
    private final List<String> allowedOrigins;

    public CsrfTokenDecryptionFilter(JweCsrfTokenRepository csrfTokenRepository, List<String> allowedOrigins) {
        this.csrfTokenRepository = csrfTokenRepository;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (requiresCsrfCheck(request)) {
            logger.debug("CSRF check required for {}", request.getRequestURI());

            String encryptedToken = request.getHeader(csrfTokenRepository.getHeaderName());
            logger.debug("Token from header: {}", encryptedToken);

            if (encryptedToken == null) {
                Cookie cookie = WebUtils.getCookie(request, csrfTokenRepository.getCookieName());
                logger.debug("Token from cookie: {}", cookie != null ? "[present]" : "null");
                if (cookie != null) {
                    encryptedToken = cookie.getValue();
                }
            }

            if (encryptedToken != null) {
                logger.debug("Attempting to decrypt token...");
                String decryptedToken = csrfTokenRepository.decryptToken(encryptedToken);

                if (decryptedToken != null) {
                    logger.debug("Token decrypted successfully");
                    request.setAttribute(csrfTokenRepository.getParameterName(), decryptedToken);
                } else {
                    logger.error("Failed to decrypt CSRF token");
                }
            } else {
                logger.warn("No CSRF token found in request");
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean requiresCsrfCheck(HttpServletRequest request) {
        String method = request.getMethod();
        return "POST".equals(method) || "PUT".equals(method)
                || "PATCH".equals(method) || "DELETE".equals(method);
    }

    private boolean isValidOrigin(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");

        // Разрешенные запросы без Origin/Referer (например, для мобильных приложений)
        if (origin == null && referer == null) {
            return true;
        }

        // Проверка Origin
        if (origin != null) {
            try {
                // Нормализуем origin (удаляем trailing slash если есть)
                origin = origin.endsWith("/") ? origin.substring(0, origin.length() - 1) : origin;
                return allowedOrigins.contains(origin);
            } catch (Exception e) {
                logger.warn("Invalid Origin header format: " + origin, e);
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
                    return false;
                }

                // Проверяем каждый allowed origin
                for (String allowedOrigin : allowedOrigins) {
                    try {
                        URI allowedUri = new URI(allowedOrigin);
                        if (refererHost.equalsIgnoreCase(allowedUri.getHost())) {
                            return true;
                        }
                    } catch (URISyntaxException e) {
                        logger.warn("Invalid allowed origin format: " + allowedOrigin, e);
                        continue;
                    }
                }
                return false;
            } catch (URISyntaxException e) {
                logger.warn("Invalid Referer header format: " + referer, e);
                return false;
            }
        }

        return false;
    }
}
