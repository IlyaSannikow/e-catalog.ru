package pro.akosarev.sandbox.configuration;

import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.*;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;
import pro.akosarev.sandbox.entity.User;
import pro.akosarev.sandbox.service.LoginAttemptService;
import pro.akosarev.sandbox.service.RecaptchaService;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Supplier;

@Configuration
public class SecurityConfig {
    @Bean
    public TokenCookieJweStringSerializer tokenCookieJweStringSerializer(
            @Value("${jwt.cookie-token-key}") String cookieTokenKey
    ) throws Exception {
        return new TokenCookieJweStringSerializer(new DirectEncrypter(
                OctetSequenceKey.parse(cookieTokenKey)
        ));
    }

    @Bean
    public TokenCookieAuthenticationConfigurer tokenCookieAuthenticationConfigurer(
            @Value("${jwt.cookie-token-key}") String cookieTokenKey,
            JdbcTemplate jdbcTemplate
    ) throws Exception {
        return new TokenCookieAuthenticationConfigurer()
                .tokenCookieStringDeserializer(new TokenCookieJweStringDeserializer(
                        new DirectDecrypter(
                                OctetSequenceKey.parse(cookieTokenKey)
                        )
                ))
                .jdbcTemplate(jdbcTemplate);
    }

    @Bean
    public JweCsrfTokenRepository jweCsrfTokenRepository(
            @Value("${jwt.csrf-token-key}") String csrfTokenKey) throws Exception {
        byte[] keyBytes = Hex.decodeHex(csrfTokenKey);
        byte[] truncatedKey = Arrays.copyOf(keyBytes, 16); // 16 байт для A128GCM
        return new JweCsrfTokenRepository(truncatedKey)
                .withTokenValiditySeconds(600) // 10 минут
                .secure(true) // только HTTPS
                .withHttpOnly(true);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            TokenCookieAuthenticationConfigurer tokenCookieAuthenticationConfigurer,
            TokenCookieJweStringSerializer tokenCookieJweStringSerializer,
            JweCsrfTokenRepository jweCsrfTokenRepository,
            LoginAttemptService loginAttemptService,
            CustomAuthenticationFailureHandler failureHandler,
            RecaptchaService recaptchaService) throws Exception {

        var tokenCookieSessionAuthenticationStrategy = new TokenCookieSessionAuthenticationStrategy();
        tokenCookieSessionAuthenticationStrategy.setTokenStringSerializer(tokenCookieJweStringSerializer);

        http
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/")
                        .failureHandler(new RecaptchaAuthenticationFailureHandler("/login?error", recaptchaService, loginAttemptService))
                        .permitAll())
                .addFilterBefore(new RecaptchaFilter(recaptchaService, loginAttemptService), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new GetCsrfTokenFilter(jweCsrfTokenRepository), ExceptionTranslationFilter.class)
                .addFilterAfter(new CsrfTokenInitializerFilter(jweCsrfTokenRepository), GetCsrfTokenFilter.class)
                .addFilterBefore(new CsrfTokenDecryptionFilter(jweCsrfTokenRepository), CsrfFilter.class) // Новый фильтр
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/public/**", "/js/**", "/resources/**", "/error",
                                "/registration", "/register", "/login", "/user-info",
                                "/changePassword", "/upload", "index.html", "/registration.html").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .sessionAuthenticationStrategy(tokenCookieSessionAuthenticationStrategy))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(jweCsrfTokenRepository)
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler(jweCsrfTokenRepository))
                        .ignoringRequestMatchers("/public/**", "/register", "/login"))
                .logout(logout -> logout
                        .logoutUrl("/logout") // только POST
                        .logoutSuccessUrl("/index.html")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                        .permitAll()
                );

        http.apply(tokenCookieAuthenticationConfigurer);

        return http.build();
    }

    private static class SpaCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {
        private final JweCsrfTokenRepository tokenRepository;

        public SpaCsrfTokenRequestHandler(JweCsrfTokenRepository tokenRepository) {
            this.tokenRepository = tokenRepository;
        }

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response,
                           Supplier<CsrfToken> csrfToken) {
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                CsrfToken token = csrfToken.get();
                if (token != null) {
                    tokenRepository.saveToken(token, request, response);
                }
            }
        }

        @Override
        public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
            // 1. Проверяем атрибуты запроса (установленные фильтром)
            String decryptedToken = (String) request.getAttribute(csrfToken.getParameterName());
            if (decryptedToken != null) {
                return decryptedToken;
            }

            // 2. Пробуем получить из куки напрямую
            Cookie cookie = WebUtils.getCookie(request, tokenRepository.getCookieName());
            if (cookie != null) {
                String encrypted = cookie.getValue();
                return tokenRepository.decryptToken(encrypted);
            }

            return null;
        }
    }

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

    @Bean
    public LoginAttemptService loginAttemptService() {
        return new LoginAttemptService();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(EntityManager entityManager, LoginAttemptService loginAttemptService) {
        return username -> {
            if (loginAttemptService.isBlocked(username)) {
                throw new LockedException("Account temporarily locked due to too many failed attempts");
            }

            try {
                User user = entityManager.createQuery("SELECT u FROM User u LEFT JOIN FETCH u.authorities WHERE u.username = :username", User.class)
                        .setParameter("username", username)
                        .getSingleResult();
                return user;
            } catch (NoResultException e) {
                throw new UsernameNotFoundException("User not found");
            }
        };
    }

    @ControllerAdvice
    public class SecurityExceptionHandler {
        @ExceptionHandler(LockedException.class)
        public ResponseEntity<String> handleLockedException(LockedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    public static class BlockedUserFilter extends OncePerRequestFilter {
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
}