package pro.akosarev.sandbox.security.blocking;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import pro.akosarev.sandbox.service.LoginAttemptService;

import java.io.IOException;

public class BlockedUserFilter extends OncePerRequestFilter {
    private final LoginAttemptService loginAttemptService;

    public BlockedUserFilter(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if ("/login".equals(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod())) {
            String username = request.getParameter("username");
            if (username != null && loginAttemptService.isBlocked(username)) {
                response.sendRedirect("/index.html");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
