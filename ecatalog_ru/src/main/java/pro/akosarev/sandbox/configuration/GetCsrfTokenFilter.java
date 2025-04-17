package pro.akosarev.sandbox.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class GetCsrfTokenFilter extends OncePerRequestFilter {
    private final RequestMatcher requestMatcher = new AntPathRequestMatcher("/csrf", HttpMethod.GET.name());
    private final JweCsrfTokenRepository csrfTokenRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GetCsrfTokenFilter(JweCsrfTokenRepository csrfTokenRepository) {
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (this.requestMatcher.matches(request)) {
            // Генерируем новый токен
            CsrfToken token = csrfTokenRepository.generateToken(request);

            // Сохраняем зашифрованный токен в куки
            csrfTokenRepository.saveToken(token, request, response);

            // Получаем зашифрованное значение токена из куки
            Cookie cookie = WebUtils.getCookie(request, csrfTokenRepository.getCookieName());
            String encryptedTokenValue = cookie != null ? cookie.getValue() : null;

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("headerName", token.getHeaderName());
            responseBody.put("parameterName", token.getParameterName());

            // Возвращаем зашифрованный токен вместо raw значения
            responseBody.put("token", encryptedTokenValue);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), responseBody);
            return;
        }
        filterChain.doFilter(request, response);
    }
}