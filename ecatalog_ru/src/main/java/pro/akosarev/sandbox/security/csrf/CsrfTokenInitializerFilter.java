package pro.akosarev.sandbox.security.csrf;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;
import pro.akosarev.sandbox.security.csrf.JweCsrfTokenRepository;

import java.io.IOException;

public class CsrfTokenInitializerFilter extends OncePerRequestFilter {
    private final JweCsrfTokenRepository tokenRepository;

    public CsrfTokenInitializerFilter(JweCsrfTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Инициализируем токен только для GET запросов, если его еще нет или он просрочен
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            CsrfToken existingToken = tokenRepository.loadToken(request);
            if (existingToken == null) {
                CsrfToken newToken = tokenRepository.generateToken(request);
                tokenRepository.saveToken(newToken, request, response);
            }
        }

        filterChain.doFilter(request, response);
    }
}