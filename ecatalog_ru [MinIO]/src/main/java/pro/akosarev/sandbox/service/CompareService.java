package pro.akosarev.sandbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import pro.akosarev.sandbox.entity.Category;
import pro.akosarev.sandbox.entity.Product;
import pro.akosarev.sandbox.entity.ProductCompare;
import pro.akosarev.sandbox.entity.User;
import pro.akosarev.sandbox.repository.CompareRepository;
import pro.akosarev.sandbox.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompareService {

    CompareRepository compareRepository;

    ProductRepository productRepository;

    ProductService productService;

    public CompareService(CompareRepository compareRepository, ProductRepository productRepository, ProductService productService){
        this.compareRepository = compareRepository;
        this.productRepository = productRepository;
        this.productService = productService;
    }

    public void saveCompare(User user, Product product){
        Optional<ProductCompare> existingCompare = compareRepository.findByUserAndProduct(user, product);

        if (existingCompare.isEmpty()) {
            ProductCompare productCompare = new ProductCompare();
            productCompare.setUser(user);
            productCompare.setProduct(product);

            try {
                compareRepository.save(productCompare);
            } catch (DataIntegrityViolationException e) {
                System.err.println("Failed to save comparison due to data integrity violation.");
            }

        }
    }

    public List<Product> getProductsByUser(User user) {

        List<ProductCompare> productCompares = compareRepository.findByUser(user);

        if (!productCompares.isEmpty()){
            List<Product> products = productCompares.stream()
                    .map(ProductCompare::getProduct)
                    .collect(Collectors.toList());

            return products;
        }

        return new ArrayList<>();
    }

    @Transactional
    public void deleteCompare(String userId, String productName) {
        compareRepository.deleteByUserIdAndProductName(userId, productName);
    }
}
