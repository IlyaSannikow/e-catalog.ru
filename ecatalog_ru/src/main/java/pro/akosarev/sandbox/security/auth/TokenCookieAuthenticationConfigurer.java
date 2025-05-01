package pro.akosarev.sandbox.security.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.csrf.CsrfFilter;
import pro.akosarev.sandbox.entity.Token;
import pro.akosarev.sandbox.entity.TokenAuthenticationUserDetailsService;
import pro.akosarev.sandbox.entity.TokenUser;
import pro.akosarev.sandbox.entity.UserLogoutEvent;
import pro.akosarev.sandbox.repository.UserLogoutEventRepository;

import java.util.Date;
import java.util.function.Function;

public class TokenCookieAuthenticationConfigurer
        extends AbstractHttpConfigurer<TokenCookieAuthenticationConfigurer, HttpSecurity> {

    private Function<String, Token> tokenCookieStringDeserializer;
    private UserLogoutEventRepository userLogoutEventRepository;

    private JdbcTemplate jdbcTemplate;

    @Override
    public void init(HttpSecurity builder) throws Exception {
        builder.logout(logout -> logout.addLogoutHandler(
                        new CookieClearingLogoutHandler("__Host-auth-token"))
                .addLogoutHandler((request, response, authentication) -> {
                    if (authentication != null &&
                            authentication.getPrincipal() instanceof TokenUser user) {
                        // Инвалидируем текущий токен
                        this.jdbcTemplate.update(
                                "insert into t_deactivated_token (id, c_keep_until) values (?, ?)",
                                user.getToken().id(), Date.from(user.getToken().expiresAt()));

                        // Записываем событие logout
                        userLogoutEventRepository.save(
                                new UserLogoutEvent(user.getUsername(), new Date()));

                        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    }
                }));
    }

    @Override
    public void configure(HttpSecurity builder) throws Exception {
        var cookieAuthenticationFilter = new AuthenticationFilter(
                builder.getSharedObject(AuthenticationManager.class),
                new TokenCookieAuthenticationConverter(this.tokenCookieStringDeserializer));
        cookieAuthenticationFilter.setSuccessHandler((request, response, authentication) -> {});
        cookieAuthenticationFilter.setFailureHandler(
                new AuthenticationEntryPointFailureHandler(
                        new Http403ForbiddenEntryPoint()
                )
        );

        var authenticationProvider = new PreAuthenticatedAuthenticationProvider();
        authenticationProvider.setPreAuthenticatedUserDetailsService(
                new TokenAuthenticationUserDetailsService(this.jdbcTemplate, this.userLogoutEventRepository));

        builder.addFilterAfter(cookieAuthenticationFilter, CsrfFilter.class)
                .authenticationProvider(authenticationProvider);
    }

    public TokenCookieAuthenticationConfigurer tokenCookieStringDeserializer(
            Function<String, Token> tokenCookieStringDeserializer) {
        this.tokenCookieStringDeserializer = tokenCookieStringDeserializer;
        return this;
    }

    public TokenCookieAuthenticationConfigurer jdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        return this;
    }

    public TokenCookieAuthenticationConfigurer userLogoutEventRepository(
            UserLogoutEventRepository userLogoutEventRepository) {
        this.userLogoutEventRepository = userLogoutEventRepository;
        return this;
    }
}
