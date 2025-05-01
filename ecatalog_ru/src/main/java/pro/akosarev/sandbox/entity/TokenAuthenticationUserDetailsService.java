package pro.akosarev.sandbox.entity;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import pro.akosarev.sandbox.repository.UserLogoutEventRepository;

import java.time.Instant;
import java.util.Date;

public class TokenAuthenticationUserDetailsService
        implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

    private final JdbcTemplate jdbcTemplate;
    private final UserLogoutEventRepository userLogoutEventRepository;

    public TokenAuthenticationUserDetailsService(JdbcTemplate jdbcTemplate, UserLogoutEventRepository userLogoutEventRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.userLogoutEventRepository = userLogoutEventRepository;
    }

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken authenticationToken)
            throws UsernameNotFoundException {
        if (authenticationToken.getPrincipal() instanceof Token token) {
            // Проверяем, не был ли токен явно деактивирован
            boolean isTokenDeactivated = this.jdbcTemplate.queryForObject("""
                    select exists(select id from t_deactivated_token where id = ?)
                    """, Boolean.class, token.id());

            // Проверяем, не было ли глобального logout для этого пользователя после создания токена
            boolean isGlobalLogout = userLogoutEventRepository.existsLogoutEventAfter(
                    token.subject(), Date.from(token.createdAt()));

            boolean isTokenValid = !isTokenDeactivated &&
                    !isGlobalLogout &&
                    token.expiresAt().isAfter(Instant.now());

            // Проверка активности пользователя
            boolean isUserActive = this.jdbcTemplate.queryForObject("""
                    select not blocked from t_user where username = ?
                    """, Boolean.class, token.subject());

            return new TokenUser(token.subject(), "nopassword", true, true,
                    isTokenValid && isUserActive,
                    true,
                    token.authorities().stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList(), token);
        }
        throw new UsernameNotFoundException("Principal must be of type Token");
    }
}
