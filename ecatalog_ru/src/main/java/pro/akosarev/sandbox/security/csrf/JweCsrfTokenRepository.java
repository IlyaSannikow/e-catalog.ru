package pro.akosarev.sandbox.security.csrf;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.web.csrf.*;
import org.springframework.web.util.*;
import pro.akosarev.sandbox.entity.UsedCsrfToken;
import pro.akosarev.sandbox.repository.UsedCsrfTokenRepository;


import java.time.Instant;
import java.util.*;
import java.text.*;

public class JweCsrfTokenRepository implements CsrfTokenRepository {
    private static final Logger logger = LoggerFactory.getLogger(JweCsrfTokenRepository.class);
    private static final String DEFAULT_CSRF_COOKIE_NAME = "XSRF-TOKEN";
    private static final String DEFAULT_CSRF_PARAMETER_NAME = "_csrf";
    private static final String DEFAULT_CSRF_HEADER_NAME = "X-XSRF-TOKEN";

    private final DirectEncrypter encrypter;
    private final DirectDecrypter decrypter;
    private String cookieName = DEFAULT_CSRF_COOKIE_NAME;
    private String parameterName = DEFAULT_CSRF_PARAMETER_NAME;
    private String headerName = DEFAULT_CSRF_HEADER_NAME;
    private boolean secure = true;
    private boolean httpOnly = true;
    private int tokenValiditySeconds = 600; // 10 минут по умолчанию

    private final UsedCsrfTokenRepository usedCsrfTokenRepository;

    public JweCsrfTokenRepository(byte[] key, UsedCsrfTokenRepository usedCsrfTokenRepository) throws Exception {
        this.usedCsrfTokenRepository = usedCsrfTokenRepository;
        OctetSequenceKey octetKey = new OctetSequenceKey.Builder(key)
                .keyID(UUID.randomUUID().toString())
                .build();

        this.encrypter = new DirectEncrypter(octetKey);
        this.decrypter = new DirectDecrypter(octetKey);
    }

    public UsedCsrfTokenRepository getUsedCsrfTokenRepository() {
        return usedCsrfTokenRepository;
    }

    public JweCsrfTokenRepository withTokenValiditySeconds(int tokenValiditySeconds) {
        this.tokenValiditySeconds = tokenValiditySeconds;
        return this;
    }

    public JweCsrfTokenRepository secure(boolean secure) {
        this.secure = secure;
        return this;
    }

    public JweCsrfTokenRepository withHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
        return this;
    }

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        invalidatePreviousTokens(request);
        return new DefaultCsrfToken(headerName, parameterName, createNewToken());
    }

    private void invalidatePreviousTokens(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, cookieName);
        if (cookie != null) {
            markTokenAsUsed(cookie.getValue());
        }
    }

    @Transactional
    public void markTokenAsUsed(String encryptedToken) {
        logger.debug("Marking token as used: {}", encryptedToken);
        if (!usedCsrfTokenRepository.existsByEncryptedToken(encryptedToken)) {
            Instant expiresAt = Instant.now().plusSeconds(tokenValiditySeconds);
            UsedCsrfToken token = new UsedCsrfToken(encryptedToken, expiresAt);
            usedCsrfTokenRepository.save(token);
            logger.debug("Token marked as used and saved to DB");
        } else {
            logger.debug("Token already marked as used");
        }
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        if (token == null) {
            response.addCookie(createExpiredCookie(request));
            return;
        }

        String encryptedToken = encryptToken(token.getToken());
        if (!usedCsrfTokenRepository.existsByEncryptedToken(encryptedToken)) {
            markTokenAsUsed(encryptedToken);
            Cookie cookie = new Cookie(cookieName, encryptedToken);
            cookie.setSecure(secure);
            cookie.setPath(getCookiePath(request));
            cookie.setHttpOnly(httpOnly);
            cookie.setMaxAge(tokenValiditySeconds);
            response.addCookie(cookie);
        }
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, cookieName);
        if (cookie == null) {
            return null;
        }

        String encryptedToken = cookie.getValue();
        String token = decryptToken(encryptedToken);
        return token != null ? new DefaultCsrfToken(headerName, parameterName, token) : null;
    }

    private String createNewToken() {
        return UUID.randomUUID().toString();
    }

    public String encryptToken(String token) {
        try {
            JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A128GCM)
                    .keyID("csrf-key")
                    .build();

            JWEObject jweObject = new JWEObject(header, new Payload(token));
            jweObject.encrypt(encrypter);

            return jweObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to encrypt CSRF token", e);
        }
    }

    public String getHeaderName() {
        return headerName;
    }

    public String getParameterName() {
        return parameterName;
    }

    public String getCookieName() {
        return cookieName;
    }

    public String decryptToken(String encryptedToken) {
        if (encryptedToken == null || encryptedToken.isEmpty()) {
            return null;
        }

        try {
            JWEObject jweObject = JWEObject.parse(encryptedToken);
            jweObject.decrypt(decrypter);
            return jweObject.getPayload().toString();
        } catch (Exception e) {
            return null;
        }
    }

    @Scheduled(fixedRate = 3600000) // Каждый час
    @Transactional
    public void cleanupUsedTokens() {
        try {
            logger.info("Starting cleanup of expired CSRF tokens...");
            Instant now = Instant.now();
            int deletedCount = usedCsrfTokenRepository.deleteExpiredTokens(now);
            logger.info("Cleanup completed. Deleted {} expired tokens.", deletedCount);
        } catch (Exception e) {
            logger.error("Failed to cleanup expired CSRF tokens", e);
        }
    }

    private Cookie createExpiredCookie(HttpServletRequest request) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setSecure(secure);
        cookie.setPath(getCookiePath(request));
        cookie.setHttpOnly(httpOnly);
        cookie.setMaxAge(0);
        return cookie;
    }

    private String getCookiePath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return contextPath.length() > 0 ? contextPath : "/";
    }
}
