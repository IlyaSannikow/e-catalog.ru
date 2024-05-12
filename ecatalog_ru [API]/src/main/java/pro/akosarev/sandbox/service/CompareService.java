package pro.akosarev.sandbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
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

    public void saveCompare(User user, String productName){
        Optional<ProductCompare> existingCompare = compareRepository.findByUserAndProductName(user, productName);

        if (existingCompare.isEmpty()) {
            ProductCompare productCompare = new ProductCompare();
            productCompare.setUser(user);
            productCompare.setProductName(productName);

            try {
                compareRepository.save(productCompare);
            } catch (DataIntegrityViolationException e) {
                System.err.println("Failed to save comparison due to data integrity violation.");
            }

        }
    }

    public List<Product> getProductsByUser(User user) throws JsonProcessingException {

        List<ProductCompare> productCompares = compareRepository.findByUser(user);

        if (!productCompares.isEmpty()){

            List<String> productNames = productCompares.stream()
                    .map(ProductCompare::getProductName)
                    .collect(Collectors.toList());

            String searchInput = productNames.stream()
                    .collect(Collectors.joining(" "));

            return productService.searchProducts(searchInput);
        }

        return new ArrayList<>();
    }

    @Transactional
    public void deleteCompare(String userId, String productName) {
        compareRepository.deleteByUserIdAndProductName(userId, productName);
    }
}
