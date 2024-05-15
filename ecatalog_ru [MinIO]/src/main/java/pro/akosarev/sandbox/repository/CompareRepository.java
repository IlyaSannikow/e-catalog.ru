package pro.akosarev.sandbox.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.akosarev.sandbox.entity.Product;
import pro.akosarev.sandbox.entity.ProductCompare;
import pro.akosarev.sandbox.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompareRepository extends JpaRepository<ProductCompare, Long> {
    List<ProductCompare> findByUser(User user);

    Optional<ProductCompare> findByUserAndProduct(User user, Product product);

    void deleteByUserIdAndProductName(String userId, String productName);
}
