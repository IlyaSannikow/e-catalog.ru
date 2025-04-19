package pro.akosarev.sandbox.security.recaptcha;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import pro.akosarev.sandbox.service.LoginAttemptService;
import pro.akosarev.sandbox.service.RecaptchaService;

import java.io.IOException;

public class RecaptchaAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private final RecaptchaService recaptchaService;
    private final LoginAttemptService loginAttemptService;

    public RecaptchaAuthenticationFailureHandler(String defaultFailureUrl,
                                                 RecaptchaService recaptchaService,
                                                 LoginAttemptService loginAttemptService) {
        super(defaultFailureUrl);
        this.recaptchaService = recaptchaService;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");
        loginAttemptService.loginFailed(username);

        if (loginAttemptService.isBlocked(username)) {
            getRedirectStrategy().sendRedirect(request, response, "/login?blocked");
            return;
        }

        super.onAuthenticationFailure(request, response, exception);
    }
}
