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

        if (!requiresCsrfCheck(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Получаем токен из заголовка или куки
        String encryptedToken = request.getHeader(csrfTokenRepository.getHeaderName());
        if (encryptedToken == null) {
            Cookie cookie = WebUtils.getCookie(request, csrfTokenRepository.getCookieName());
            if (cookie != null) {
                encryptedToken = cookie.getValue();
            }
        }

        // Блокируем запросы без токена
        if (encryptedToken == null) {
            logger.warn("Blocked CSRF attack - missing token for {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Missing CSRF token");
            return;
        }

        // Дешифруем и проверяем токен
        String decryptedToken = csrfTokenRepository.decryptToken(encryptedToken);
        if (decryptedToken == null) {
            logger.warn("Blocked CSRF attack - invalid token format");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
            return;
        }

        // Проверяем, не использовался ли токен ранее
        if (csrfTokenRepository.getUsedCsrfTokenRepository().existsByEncryptedToken(encryptedToken)) {
            logger.warn("Blocked CSRF attack - reused token");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token already used");
            return;
        }

        logger.info("Valid CSRF token received. Encrypted: {}, Decrypted: {}",
                encryptedToken, decryptedToken);
        request.setAttribute(csrfTokenRepository.getParameterName(), decryptedToken);

        filterChain.doFilter(request, response);
    }

    private boolean requiresCsrfCheck(HttpServletRequest request) {
        String method = request.getMethod();
        return "POST".equals(method) || "PUT".equals(method)
                || "PATCH".equals(method) || "DELETE".equals(method);
    }
}
