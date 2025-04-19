package pro.akosarev.sandbox.security.recaptcha;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import pro.akosarev.sandbox.service.LoginAttemptService;
import pro.akosarev.sandbox.service.RecaptchaService;

import java.io.IOException;

public class RecaptchaFilter extends OncePerRequestFilter {
    private final RecaptchaService recaptchaService;
    private final LoginAttemptService loginAttemptService;

    public RecaptchaFilter(RecaptchaService recaptchaService, LoginAttemptService loginAttemptService) {
        this.recaptchaService = recaptchaService;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if ("/login".equals(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod())) {
            String username = request.getParameter("username");

            if (username != null && loginAttemptService.isBlocked(username)) {
                response.sendRedirect("/login?blocked");
                return;
            }

            String recaptchaResponse = request.getParameter("g-recaptcha-response");
            if (!recaptchaService.validateRecaptcha(recaptchaResponse)) {
                response.sendRedirect("/login?error=captcha");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
