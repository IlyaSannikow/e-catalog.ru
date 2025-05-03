package pro.akosarev.sandbox.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.akosarev.sandbox.entity.UserLoginEvent;

import java.util.List;

public interface UserLoginEventRepository extends JpaRepository<UserLoginEvent, String> {
    boolean existsByTokenAndUserId(String token, String userId);
    boolean existsByToken(String token);
}
