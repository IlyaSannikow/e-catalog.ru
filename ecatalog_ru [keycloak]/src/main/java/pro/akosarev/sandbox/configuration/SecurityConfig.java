package pro.akosarev.sandbox.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.stream.Stream;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, LogoutSuccessHandler logoutSuccessHandler) throws Exception {
        return http
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))) // Здесь связываем converter
                .oauth2Login(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/index.html").permitAll()
                            .requestMatchers("/admin.html").hasRole("ADMIN")
                            .anyRequest().authenticated();
                })
                .logout(logout -> {
                    logout.logoutSuccessHandler(logoutSuccessHandler);
                })
                .build();
    }

    @Bean
    LogoutSuccessHandler logoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository){
        OidcClientInitiatedLogoutSuccessHandler handler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        handler.setPostLogoutRedirectUri("{baseUrl}/index.html");
        return handler;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var converter = new JwtAuthenticationConverter();
        var jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        converter.setPrincipalClaimName("preferred_username");
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            var authorities = jwtGrantedAuthoritiesConverter.convert(jwt);
            var roles = jwt.getClaimAsStringList("ecatalog_sec_role");
            return Stream.concat(authorities.stream(),
                            roles.stream()
                                    .filter(role -> role.startsWith("ROLE_"))
                                    .map(SimpleGrantedAuthority::new)
                                    .map(GrantedAuthority.class::cast))
                    .toList();
        });

        return converter;
    }

    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oAuth2UserService() {
        var oidcUserService = new OidcUserService();
        return userRequest -> {
            var oidcUser = oidcUserService.loadUser(userRequest);
            var roles = oidcUser.getClaimAsStringList("ecatalog_sec_role");
            var authorities = Stream.concat(oidcUser.getAuthorities().stream(),
                            roles.stream()
                                    .filter(role -> role.startsWith("ROLE_"))
                                    .map(SimpleGrantedAuthority::new)
                                    .map(GrantedAuthority.class::cast))
                    .toList();

            return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
        };
    }
}
