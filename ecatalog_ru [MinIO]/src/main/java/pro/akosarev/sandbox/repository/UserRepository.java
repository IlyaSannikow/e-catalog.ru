package pro.akosarev.sandbox.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.akosarev.sandbox.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

}
