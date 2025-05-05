package pro.akosarev.sandbox.entity;

import java.util.HashMap;
import java.util.Map;

public class RateLimitEndpointConfig {
    private RateLimitConfig register;
    private RateLimitConfig upload;

    // Геттеры и сеттеры
    public RateLimitConfig getRegister() {
        return register;
    }

    public void setRegister(RateLimitConfig register) {
        this.register = register;
    }

    public RateLimitConfig getUpload() {
        return upload;
    }

    public void setUpload(RateLimitConfig upload) {
        this.upload = upload;
    }
}
