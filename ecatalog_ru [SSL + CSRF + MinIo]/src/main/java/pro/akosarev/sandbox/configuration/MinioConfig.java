package pro.akosarev.sandbox.configuration;

import io.minio.MinioClient;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;

@Configuration
public class MinioConfig {
    private static final Logger logger = LoggerFactory.getLogger(MinioConfig.class);

    @Value("${minio.url}")
    private String url;

    @Value("${minio.access.name}")
    private String accessKey;

    @Value("${minio.access.secret}")
    private String accessSecret;

    @Value("${minio.truststore.path}")
    private String truststorePath;

    @Value("${minio.truststore.password}")
    private String truststorePassword;

    @Bean
    public MinioClient minioClient() throws Exception {
        logger.info("Initializing MinioClient with URL: {}", url);

        // 1. Проверяем, использует ли URL HTTPS
        if (!url.startsWith("https://")) {
            logger.warn("Minio URL uses HTTP instead of HTTPS! This is insecure. URL: {}", url);
            // В продакшене здесь нужно выбросить исключение
            // throw new IllegalStateException("Minio must use HTTPS");
        }

        // 2. Настраиваем TrustManager с логированием
        TrustManager[] trustManagers = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        logger.debug("Getting accepted issuers");
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                        logger.debug("Checking client trusted: authType={}", authType);
                        if (certs != null) {
                            for (X509Certificate cert : certs) {
                                logger.debug("Client cert: SubjectDN={}, IssuerDN={}, Serial={}",
                                        cert.getSubjectDN(), cert.getIssuerDN(), cert.getSerialNumber());
                            }
                        }
                        // В реальном приложении здесь должна быть проверка сертификата
                        // throw new CertificateException("Client certificate not trusted");
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                        logger.info("Checking server trusted: authType={}", authType);
                        if (certs == null || certs.length == 0) {
                            throw new CertificateException("No server certificates provided");
                        }

                        // Логируем информацию о сертификате
                        X509Certificate serverCert = certs[0];
                        logger.info("Server certificate details:");
                        logger.info("  Subject DN: {}", serverCert.getSubjectDN());
                        logger.info("  Issuer DN: {}", serverCert.getIssuerDN());
                        logger.info("  Serial Number: {}", serverCert.getSerialNumber());
                        logger.info("  Valid From: {}", serverCert.getNotBefore());
                        logger.info("  Valid Until: {}", serverCert.getNotAfter());
                        logger.info("  Sig Alg Name: {}", serverCert.getSigAlgName());
                        logger.info("  Version: {}", serverCert.getVersion());

                        try {
                            serverCert.checkValidity();
                            logger.info("Certificate is valid for current date");
                        } catch (CertificateException e) {
                            logger.error("Certificate validation failed: {}", e.getMessage());
                            throw e;
                        }

                        // Проверяем имя хоста (CN или SAN)
                        String hostname = url.replaceFirst("https?://", "").split(":")[0];
                        if (!isCertificateMatchesHostname(serverCert, hostname)) {
                            String msg = String.format("Certificate does not match hostname: %s", hostname);
                            logger.error(msg);
                            throw new CertificateException(msg);
                        }

                        // В реальном приложении здесь должна быть проверка цепочки доверия
                        logger.warn("Skipping full certificate chain validation - not safe for production!");
                    }
                }
        };

        // 3. Настраиваем SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagers, new java.security.SecureRandom());

        // 4. Настраиваем OkHttpClient
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagers[0])
                .hostnameVerifier((hostname, session) -> {
                    logger.info("Verifying hostname: {} against session: {}", hostname, session.getPeerHost());
                    // В реальном приложении нужно проверять соответствие
                    return true; // Только для разработки!
                })
                .build();

        logger.info("Creating MinioClient instance");
        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, accessSecret)
                .httpClient(httpClient)
                .build();
    }

    private boolean isCertificateMatchesHostname(X509Certificate cert, String hostname) {
        try {
            // Проверка Subject Alternative Names (SAN)
            Collection<List<?>> sans = cert.getSubjectAlternativeNames();
            if (sans != null) {
                for (List<?> san : sans) {
                    Integer type = (Integer) san.get(0);
                    if (type == 2 || type == 7) { // DNS (2) или IP (7)
                        String value = (String) san.get(1);
                        if (value.equalsIgnoreCase(hostname)) {
                            return true;
                        }
                    }
                }
            }

            // Проверка Common Name (CN)
            String dn = cert.getSubjectX500Principal().getName();
            for (String part : dn.split(",")) {
                if (part.trim().startsWith("CN=")) {
                    String cn = part.trim().substring(3);
                    return cn.equalsIgnoreCase(hostname);
                }
            }
        } catch (Exception e) {
            logger.error("Error checking hostname match: {}", e.getMessage());
        }
        return false;
    }
}
