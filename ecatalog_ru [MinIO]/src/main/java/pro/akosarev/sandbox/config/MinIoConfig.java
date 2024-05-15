package pro.akosarev.sandbox.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MinIoConfig {
    @Bean
    @Primary
    public MinioClient minioClient() {
        return new MinioClient.Builder()
                .endpoint("http://127.0.0.1:9000")
                .credentials("admin", "adminpass")
                .build();
    }
}
