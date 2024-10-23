package pro.akosarev.sandbox.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;

@Service
public class KeycloakService {

    @Value("${keycloak.auth-server-url:http://localhost:8282}")
    private String keycloakAuthServerUrl;

    @Value("${keycloak.realm:ecatalog_realm}")
    private String realm;

    public void logoutFromKeycloak(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        String url = String.format("%s/realms/%s/protocol/openid-connect/logout", keycloakAuthServerUrl, realm);

        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("client_id", "ecatalog_client"); // Замените на ваш client_id
        request.add("client_secret", "0KLlaeuaNicSmGQ45DkXTvdo7zw8oaFj"); // Добавьте client_secret если необходимо
        request.add("refresh_token", accessToken); // Это должен быть refresh token

        restTemplate.postForEntity(url, request, Void.class);
    }
}
