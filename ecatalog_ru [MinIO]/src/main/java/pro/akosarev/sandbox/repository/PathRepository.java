package pro.akosarev.sandbox.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.akosarev.sandbox.entity.Path;

@Repository
public interface PathRepository extends JpaRepository<Path, String> {
    Path findByName(String name);
}
