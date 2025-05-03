package pro.akosarev.sandbox.security.csrf;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;
import pro.akosarev.sandbox.security.csrf.JweCsrfTokenRepository;

import java.io.IOException;
import java.time.Instant;

public class CsrfTokenInitializerFilter extends OncePerRequestFilter {
    private final JweCsrfTokenRepository tokenRepository;

    public CsrfTokenInitializerFilter(JweCsrfTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            // Пропускаем статические ресурсы и запросы после аутентификации
            if (!request.getRequestURI().startsWith("/js/") &&
                    !request.getRequestURI().startsWith("/css/") &&
                    !request.getRequestURI().startsWith("/images/") &&
                    !request.getRequestURI().equals("/login")) {

                // Проверяем аутентификацию пользователя
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
                    CsrfToken existingToken = tokenRepository.loadToken(request);
                    if (existingToken == null || isTokenUsedOrExpired(existingToken.getToken())) {
                        CsrfToken newToken = tokenRepository.generateToken(request);
                        if (newToken != null && WebUtils.getCookie(request, tokenRepository.getCookieName()) == null) {
                            tokenRepository.saveToken(newToken, request, response);
                        }
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isTokenUsedOrExpired(String token) {
        if (token == null) {
            return true;
        }
        try {
            String encryptedToken = tokenRepository.encryptToken(token);
            return tokenRepository.getUsedCsrfTokenRepository()
                    .existsByEncryptedToken(encryptedToken);
        } catch (Exception e) {
            return true;
        }
    }
}