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

public class CsrfTokenDecryptionFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(CsrfTokenDecryptionFilter.class);

    private final JweCsrfTokenRepository csrfTokenRepository;

    public CsrfTokenDecryptionFilter(JweCsrfTokenRepository csrfTokenRepository) {
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (requiresCsrfCheck(request)) {
            String encryptedToken = request.getHeader(csrfTokenRepository.getHeaderName());

            if (encryptedToken == null) {
                Cookie cookie = WebUtils.getCookie(request, csrfTokenRepository.getCookieName());
                if (cookie != null) {
                    encryptedToken = cookie.getValue();
                }
            }

            if (encryptedToken != null) {
                String decryptedToken = csrfTokenRepository.decryptToken(encryptedToken);
                if (decryptedToken != null) {
                    request.setAttribute(csrfTokenRepository.getParameterName(), decryptedToken);
                } else {
                    logger.error("Failed to decrypt CSRF token");
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean requiresCsrfCheck(HttpServletRequest request) {
        String method = request.getMethod();
        return "POST".equals(method) || "PUT".equals(method)
                || "PATCH".equals(method) || "DELETE".equals(method);
    }
}