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
    Product findProductById(Long id);
    Product findProductByName(String name);
    List<Product> findByName(String name);
    List<Product> findByCategory(Category category);
    List<Product> findByNameAndCategory(String name, Category category);
}
