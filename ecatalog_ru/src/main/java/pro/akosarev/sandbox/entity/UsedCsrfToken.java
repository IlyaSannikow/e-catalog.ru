package pro.akosarev.sandbox.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "used_csrf_tokens")
public class UsedCsrfToken {

    @Id
    @Column(name = "token_id", length = 36)
    private String id;

    @Column(name = "encrypted_token", length = 500, nullable = false, unique = true)
    private String encryptedToken;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    // Конструкторы, геттеры и сеттеры
    public UsedCsrfToken() {
    }

    public UsedCsrfToken(String encryptedToken, Instant expiresAt) {
        this.id = UUID.randomUUID().toString();
        this.encryptedToken = encryptedToken;
        this.createdAt = Instant.now();
        this.expiresAt = expiresAt;
    }

    // Геттеры и сеттеры
    public String getId() {
        return id;
    }

    public String getEncryptedToken() {
        return encryptedToken;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}
