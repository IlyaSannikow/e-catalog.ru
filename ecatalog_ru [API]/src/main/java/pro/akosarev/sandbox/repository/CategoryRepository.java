package pro.akosarev.sandbox.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pro.akosarev.sandbox.entity.Category;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findById(Long id);
    Category findByName(String name);
    @Query("select name from #{#entityName}")
    public List<String> findAllCategories();

    @Query( "select id from #{#entityName}" )
    public List<Long> findAllId();
}
