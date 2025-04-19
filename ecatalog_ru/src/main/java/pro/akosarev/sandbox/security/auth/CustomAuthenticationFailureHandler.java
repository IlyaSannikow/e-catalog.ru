package pro.akosarev.sandbox.security.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import pro.akosarev.sandbox.service.LoginAttemptService;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private final LoginAttemptService loginAttemptService;

    public CustomAuthenticationFailureHandler(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String username = request.getParameter("username");
        loginAttemptService.loginFailed(username);

        if (loginAttemptService.isBlocked(username)) {
            getRedirectStrategy().sendRedirect(request, response, "/index.html");
        } else {
            getRedirectStrategy().sendRedirect(request, response, "/login");
        }
    }
}
