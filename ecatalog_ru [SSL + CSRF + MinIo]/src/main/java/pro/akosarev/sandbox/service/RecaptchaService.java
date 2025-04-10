package pro.akosarev.sandbox.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class RecaptchaService {
    @Value("${google.recaptcha.secret}")
    private String secret;

    @Value("${google.recaptcha.url}")
    private String url;

    public boolean validateRecaptcha(String recaptchaResponse) {
        if (recaptchaResponse == null || recaptchaResponse.isEmpty()) {
            return false;
        }

        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("secret", secret);
        requestMap.add("response", recaptchaResponse);

        RecaptchaResponse response = restTemplate.postForObject(url, requestMap, RecaptchaResponse.class);
        return response != null && response.isSuccess();
    }

    private static class RecaptchaResponse {
        // геттеры и сеттеры
        @Setter
        @Getter
        private boolean success;
        private String challenge_ts;
        private String hostname;
    }
}
