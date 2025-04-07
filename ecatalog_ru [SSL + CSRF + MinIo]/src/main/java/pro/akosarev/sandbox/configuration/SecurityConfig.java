package pro.akosarev.sandbox.configuration;

import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.csrf.*;
import org.springframework.web.util.WebUtils;
import pro.akosarev.sandbox.entity.User;

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
            JweCsrfTokenRepository jweCsrfTokenRepository) throws Exception {

        var tokenCookieSessionAuthenticationStrategy = new TokenCookieSessionAuthenticationStrategy();
        tokenCookieSessionAuthenticationStrategy.setTokenStringSerializer(tokenCookieJweStringSerializer);

        http
                .httpBasic(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults())
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
                        .ignoringRequestMatchers("/public/**", "/register", "/login", "/logout"))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/index.html")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN"));

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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(EntityManager entityManager) {
        return username -> {
            User user = entityManager.createQuery("SELECT u FROM User u LEFT JOIN FETCH u.authorities WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return user;
        };
    }
}