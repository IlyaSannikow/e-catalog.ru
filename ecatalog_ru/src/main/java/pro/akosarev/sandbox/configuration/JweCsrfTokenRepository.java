package pro.akosarev.sandbox.configuration;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.gen.*;
import com.nimbusds.jwt.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.csrf.*;
import org.springframework.web.util.*;


import java.util.*;
import java.text.*;

public class JweCsrfTokenRepository implements CsrfTokenRepository {
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

    public JweCsrfTokenRepository(byte[] key) throws Exception {
        OctetSequenceKey octetKey = new OctetSequenceKey.Builder(key)
                .keyID(UUID.randomUUID().toString())
                .build();

        this.encrypter = new DirectEncrypter(octetKey);
        this.decrypter = new DirectDecrypter(octetKey);

        // Проверка работы шифрования/дешифрования
        String testToken = "test_token";
        String encrypted = encryptToken(testToken);
        String decrypted = decryptToken(encrypted);

        if (!testToken.equals(decrypted)) {
            throw new IllegalStateException("Encryption/decryption test failed");
        }
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
        return new DefaultCsrfToken(headerName, parameterName, createNewToken());
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        if (token == null) {
            response.addCookie(createExpiredCookie(request));
            return;
        }

        String encryptedToken = encryptToken(token.getToken());
        Cookie cookie = new Cookie(cookieName, encryptedToken);
        cookie.setSecure(secure);
        cookie.setPath(getCookiePath(request));
        cookie.setHttpOnly(httpOnly);
        cookie.setMaxAge(tokenValiditySeconds);
        response.addCookie(cookie);
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
            JWEObject jweObject = new JWEObject(
                    new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A128GCM).build(),
                    new Payload(token));
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
        try {
            JWEObject jweObject = JWEObject.parse(encryptedToken);
            jweObject.decrypt(decrypter);
            return jweObject.getPayload().toString();
        } catch (ParseException | JOSEException e) {
            return null;
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
