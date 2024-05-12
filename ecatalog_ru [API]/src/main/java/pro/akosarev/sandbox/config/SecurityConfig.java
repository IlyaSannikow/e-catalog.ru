package pro.akosarev.sandbox.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, HttpServletRequest httpServletRequest) throws Exception {
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
        http.oauth2Login(withDefaults());

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(c -> c
                        .requestMatchers("/resources/**","/index.html","/index").permitAll()
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/create-product", "/products", "/product-info",
                                "/product-update", "/comment-update", "/compare-products").hasRole("USER")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedPage("/access-denied.html") // Перенаправление на access-denied.html при отказе в доступе
                )
                .build();
    }
}
