package pro.akosarev.sandbox.configuration;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SslOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

@Configuration
public class RedisConfig {
    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${redis.url}")
    private String url;

    @Value("${redis.password}")
    private String password;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        logger.info("Initializing Redis connection with URL: {}", url);

        // Извлекаем хост и порт из URL
        String[] parts = url.replaceFirst("rediss?://", "").split(":");
        String host = parts[0];
        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 6379;

        // Настраиваем подключение к Redis
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(host, port);
        if (password != null && !password.isEmpty()) {
            serverConfig.setPassword(password);
        }

        // Создаем SSL конфигурацию
        SslOptions sslOptions = SslOptions.builder()
                .trustManager(InsecureTrustManagerFactory.INSTANCE) // Только для разработки!
                .build();

        ClientOptions clientOptions = ClientOptions.builder()
                .protocolVersion(ProtocolVersion.RESP2)
                .sslOptions(sslOptions)
                .build();

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(30))
                .clientOptions(clientOptions)
                .useSsl()
                .build();

        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(serverConfig, clientConfig);
        connectionFactory.setValidateConnection(true);
        return connectionFactory;
    }

    @Bean
    public CommandLineRunner testRedisConnection(RedisTemplate<String, String> redisTemplate) {
        return args -> {
            try {
                logger.info("Testing Redis connection...");
                redisTemplate.opsForValue().set("testKey", "testValue");
                String value = redisTemplate.opsForValue().get("testKey");
                logger.info("Retrieved value from Redis: {}", value);
                redisTemplate.delete("testKey");
                logger.info("Redis connection test successful!");
            } catch (Exception e) {
                logger.error("Redis connection test failed", e);
            }
        };
    }
}