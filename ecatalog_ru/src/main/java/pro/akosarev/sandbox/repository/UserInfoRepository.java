package pro.akosarev.sandbox.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.akosarev.sandbox.entity.UserInfo;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {

}
