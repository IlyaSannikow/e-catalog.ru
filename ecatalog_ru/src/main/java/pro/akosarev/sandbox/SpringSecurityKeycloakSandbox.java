package pro.akosarev.sandbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import pro.akosarev.sandbox.configuration.SecurityProperties;

@SpringBootApplication
@EnableConfigurationProperties(SecurityProperties.class)
@EnableScheduling
public class SpringSecurityKeycloakSandbox {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(SpringSecurityKeycloakSandbox.class, args);
    }
}
