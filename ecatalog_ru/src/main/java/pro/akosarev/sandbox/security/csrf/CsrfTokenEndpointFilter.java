package pro.akosarev.sandbox.security.csrf;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class CsrfTokenEndpointFilter extends OncePerRequestFilter {
    private final JweCsrfTokenRepository csrfTokenRepository;
    private static final Logger logger = LoggerFactory.getLogger(CsrfTokenEndpointFilter.class);

    public CsrfTokenEndpointFilter(JweCsrfTokenRepository csrfTokenRepository) {
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if ("/csrf-token".equals(request.getRequestURI()) && "GET".equalsIgnoreCase(request.getMethod())) {
            try {
                // Логируем текущий токен (если есть) перед генерацией нового
                CsrfToken currentToken = csrfTokenRepository.loadToken(request);
                if (currentToken != null) {
                    System.out.println("Current CSRF token (encrypted):" +  currentToken.getToken());
                } else {
                    System.out.println("No existing CSRF token found");
                }

                // Генерируем и сохраняем новый токен
                CsrfToken newToken = csrfTokenRepository.generateToken(request);
                csrfTokenRepository.saveToken(newToken, request, response);

                response.setContentType("application/json");
                response.getWriter().write("{\"status\":\"ok\"}");
                return;
            } catch (Exception e) {
                logger.error("Failed to generate CSRF token", e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"Failed to generate CSRF token\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}