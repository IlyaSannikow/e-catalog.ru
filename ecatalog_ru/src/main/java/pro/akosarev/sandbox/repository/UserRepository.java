package pro.akosarev.sandbox.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.akosarev.sandbox.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    boolean existsByUsername(String username);
}
