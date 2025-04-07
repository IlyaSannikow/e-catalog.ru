package pro.akosarev.sandbox.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.akosarev.sandbox.entity.UserAuthority;

import java.util.List;

public interface UserAuthorityRepository extends JpaRepository<UserAuthority, Long> {
}
