package pro.akosarev.sandbox.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.akosarev.sandbox.entity.UsedCsrfToken;

import java.time.Instant;
import java.util.Optional;

public interface UsedCsrfTokenRepository extends JpaRepository<UsedCsrfToken, String> {

    boolean existsByEncryptedToken(String encryptedToken);

    boolean existsByEncryptedTokenAndExpiresAtAfter(String encryptedToken, Instant expiresAt);

    @Modifying
    @Query("DELETE FROM UsedCsrfToken t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") Instant now);
}
