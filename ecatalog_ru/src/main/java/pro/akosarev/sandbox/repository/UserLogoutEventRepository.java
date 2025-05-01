package pro.akosarev.sandbox.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.akosarev.sandbox.entity.UserLogoutEvent;

import java.util.Date;

@Repository
public interface UserLogoutEventRepository extends JpaRepository<UserLogoutEvent, Long> {

    @Query("SELECT COUNT(e) > 0 FROM UserLogoutEvent e WHERE e.userId = :userId AND e.logoutTime > :createdAt")
    boolean existsLogoutEventAfter(@Param("userId") String userId, @Param("createdAt") Date createdAt);

    @Query("SELECT COUNT(e) > 0 FROM UserLogoutEvent e WHERE e.token = :token")
    boolean existsByToken(@Param("token") String token);

    @Modifying
    @Query("DELETE FROM UserLogoutEvent e WHERE e.logoutTime < :cutoffDate")
    void deleteOldEvents(@Param("cutoffDate") Date cutoffDate);
}
