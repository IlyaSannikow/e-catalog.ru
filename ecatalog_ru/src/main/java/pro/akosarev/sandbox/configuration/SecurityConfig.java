package pro.akosarev.sandbox.configuration;

import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Hex;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.*;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;
import pro.akosarev.sandbox.entity.User;
import pro.akosarev.sandbox.entity.UserLoginEvent;
import pro.akosarev.sandbox.entity.UserLogoutEvent;
import pro.akosarev.sandbox.repository.UsedCsrfTokenRepository;
import pro.akosarev.sandbox.repository.UserLoginEventRepository;
import pro.akosarev.sandbox.repository.UserLogoutEventRepository;
import pro.akosarev.sandbox.security.auth.CustomAuthenticationFailureHandler;
import pro.akosarev.sandbox.security.auth.TokenCookieAuthenticationConfigurer;
import pro.akosarev.sandbox.security.cookie.TokenCookieJweStringDeserializer;
import pro.akosarev.sandbox.security.cookie.TokenCookieJweStringSerializer;
import pro.akosarev.sandbox.security.cookie.TokenCookieSessionAuthenticationStrategy;
import pro.akosarev.sandbox.security.csrf.*;
import pro.akosarev.sandbox.security.recaptcha.RecaptchaAuthenticationFailureHandler;
import pro.akosarev.sandbox.security.recaptcha.RecaptchaFilter;
import pro.akosarev.sandbox.service.LoginAttemptService;
import pro.akosarev.sandbox.service.RecaptchaService;
import pro.akosarev.sandbox.service.TokenValidationService;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

@Configuration
public class SecurityConfig {

    private final SecurityProperties securityProperties;
    private final UserLogoutEventRepository userLogoutEventRepository;
    private final UserLoginEventRepository userLoginEventRepository;
    private List<String> allowedOrigins;

    @PostConstruct
    public void init() {
        this.allowedOrigins = securityProperties.getAllowedOrigins();
    }

    public SecurityConfig(SecurityProperties securityProperties,
                          UserLogoutEventRepository userLogoutEventRepository, UserLoginEventRepository userLoginEventRepository) {
        this.securityProperties = securityProperties;
        this.userLogoutEventRepository = userLogoutEventRepository;
        this.userLoginEventRepository = userLoginEventRepository;
    }

    @Bean
    public TokenValidationService tokenValidationService(JdbcTemplate jdbcTemplate) {
        return new TokenValidationService(jdbcTemplate, allowedOrigins,
                userLogoutEventRepository, userLoginEventRepository);
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${jwt.secret-key}") String secretKey) {
        return NimbusJwtDecoder.withSecretKey(
                new SecretKeySpec(secretKey.getBytes(), "HS256")
        ).build();
    }

    @Bean
    public TokenCookieJweStringSerializer tokenCookieJweStringSerializer(
            @Value("${jwt.cookie-token-key}") String cookieTokenKey) throws Exception {
        return new TokenCookieJweStringSerializer(new DirectEncrypter(
                OctetSequenceKey.parse(cookieTokenKey)
        ));
    }

    @Bean
    public TokenCookieAuthenticationConfigurer tokenCookieAuthenticationConfigurer(
            @Value("${jwt.cookie-token-key}") String cookieTokenKey,
            JdbcTemplate jdbcTemplate,
            UserLogoutEventRepository userLogoutEventRepository,
            UserLoginEventRepository userLoginEventRepository) throws Exception {
        return new TokenCookieAuthenticationConfigurer()
                .tokenCookieStringDeserializer(new TokenCookieJweStringDeserializer(
                        new DirectDecrypter(
                                OctetSequenceKey.parse(cookieTokenKey)
                        )
                ))
                .jdbcTemplate(jdbcTemplate)
                .userLogoutEventRepository(userLogoutEventRepository)
                .userLoginEventRepository(userLoginEventRepository);
    }

    @Bean
    public JweCsrfTokenRepository jweCsrfTokenRepository(
            @Value("${jwt.csrf-token-key}") String csrfTokenKey,
            UsedCsrfTokenRepository usedCsrfTokenRepository) throws Exception {
        byte[] keyBytes = Hex.decodeHex(csrfTokenKey);
        byte[] truncatedKey = Arrays.copyOf(keyBytes, 16);
        return new JweCsrfTokenRepository(truncatedKey, usedCsrfTokenRepository)
                .withTokenValiditySeconds(600)
                .secure(true)
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
            RecaptchaService recaptchaService,
            TokenValidationService tokenValidationService,
            JwtDecoder jwtDecoder) throws Exception {

        var tokenCookieSessionAuthenticationStrategy = new TokenCookieSessionAuthenticationStrategy();
        tokenCookieSessionAuthenticationStrategy.setTokenStringSerializer(tokenCookieJweStringSerializer);

        http.apply(tokenCookieAuthenticationConfigurer);

        http
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/")
                        .failureHandler(new RecaptchaAuthenticationFailureHandler("/login?error", recaptchaService, loginAttemptService))
                        .successHandler((request, response, authentication) -> {
                            response.sendRedirect("/");
                        })
                        .permitAll())
                .addFilterBefore(new CsrfTokenEndpointFilter(jweCsrfTokenRepository), CsrfFilter.class)
                .addFilterBefore(new RecaptchaFilter(recaptchaService, loginAttemptService), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new CsrfTokenInitializerFilter(jweCsrfTokenRepository), ExceptionTranslationFilter.class)
                .addFilterBefore(new CsrfTokenDecryptionFilter(jweCsrfTokenRepository, allowedOrigins), CsrfFilter.class)
                .addFilterBefore(new TokenValidationFilter(tokenValidationService, jwtDecoder), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/public/**", "/js/**", "/resources/**",
                                "/error", "/register", "/login", "/registration", "/", "/csrf-token",
                                "index.html", "/registration.html").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .sessionAuthenticationStrategy(tokenCookieSessionAuthenticationStrategy))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(jweCsrfTokenRepository)
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler(jweCsrfTokenRepository))
                        .ignoringRequestMatchers("/public/**", "/csrf-token")
                        .requireCsrfProtectionMatcher(request -> {
                            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                            if (auth == null || !auth.isAuthenticated() ||
                                    auth instanceof AnonymousAuthenticationToken) {
                                return false;
                            }

                            String method = request.getMethod();
                            return "POST".equals(method) || "PUT".equals(method) ||
                                    "PATCH".equals(method) || "DELETE".equals(method);
                        }))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                        .addLogoutHandler((request, response, authentication) -> {
                            if (authentication != null) {
                                String token = extractToken(request);
                                if (token != null) {
                                    UserLogoutEvent event = new UserLogoutEvent();
                                    event.setUserId(authentication.getName());
                                    event.setToken(token);
                                    event.setLogoutTime(new Date());
                                    userLogoutEventRepository.save(event);
                                }
                            }
                        })
                        .permitAll()
                );

        return http.build();
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private static class SpaCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {
        private final JweCsrfTokenRepository tokenRepository;

        public SpaCsrfTokenRequestHandler(JweCsrfTokenRepository tokenRepository) {
            this.tokenRepository = tokenRepository;
        }

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response,
                           Supplier<CsrfToken> csrfTokenSupplier) {
            CsrfToken csrfToken = csrfTokenSupplier.get();

            // Если токен не null и еще не установлен в атрибуты запроса
            if (csrfToken != null && request.getAttribute(csrfToken.getParameterName()) == null) {
                request.setAttribute(csrfToken.getParameterName(), csrfToken.getToken());
            }
        }

        @Override
        public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
            if (csrfToken == null) {
                return null;
            }

            // 1. Проверяем атрибуты запроса (установленные фильтром)
            String decryptedToken = (String) request.getAttribute(csrfToken.getParameterName());
            if (decryptedToken != null) {
                return decryptedToken;
            }

            // 2. Получаем из куки
            Cookie cookie = WebUtils.getCookie(request, tokenRepository.getCookieName());
            if (cookie != null) {
                String token = tokenRepository.decryptToken(cookie.getValue());
                if (token != null) {
                    return token;
                }
            }

            return null;
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
}