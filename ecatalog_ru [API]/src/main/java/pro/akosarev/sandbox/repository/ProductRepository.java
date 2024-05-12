package pro.akosarev.sandbox.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.akosarev.sandbox.entity.Category;
import pro.akosarev.sandbox.entity.Product;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findById(Long id);
    Product findByName(String name);
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByCategoryNameContainingIgnoreCase(String name);

    List<Product> findByNameIn(List<String> productNames);
}
