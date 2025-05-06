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
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {
    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${redis.url}")
    private String url;

    @Value("${redis.password}")
    private String password;

    @Value("${redis.admin.pool.size:5}")
    private int adminPoolSize;

    @Primary
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return createConnectionFactory(50, 20, 5); // Стандартные настройки пула
    }

    // Отдельный пул соединений для администраторов
    @Bean(name = "adminRedisConnectionFactory")
    public LettuceConnectionFactory adminRedisConnectionFactory() {
        return createConnectionFactory(adminPoolSize, adminPoolSize, adminPoolSize);
    }

    private LettuceConnectionFactory createConnectionFactory(int maxActive, int maxIdle, int minIdle) {
        logger.info("Initializing Redis connection with URL: {}", url);

        String[] parts = url.replaceFirst("rediss?://", "").split(":");
        String host = parts[0];
        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 6379;

        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(host, port);
        if (password != null && !password.isEmpty()) {
            serverConfig.setPassword(password);
        }

        SslOptions sslOptions = SslOptions.builder()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
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

    // Основной RedisTemplate
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    // RedisTemplate для администраторов
    @Bean(name = "adminRedisTemplate")
    public RedisTemplate<String, Object> adminRedisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(adminRedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}