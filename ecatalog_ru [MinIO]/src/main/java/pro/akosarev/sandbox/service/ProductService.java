package pro.akosarev.sandbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Data;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pro.akosarev.sandbox.entity.*;
import pro.akosarev.sandbox.repository.PathRepository;
import pro.akosarev.sandbox.repository.ProductRepository;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

@Service
public class ProductService  {
    @PersistenceContext
    private EntityManager em;
    ProductRepository productRepository;
    PathRepository pathRepository;
    CategoryService categoryService;
    MinIoService minIoService;

    JsonService jsonService;
    public ProductService(ProductRepository productRepository, PathRepository pathRepository, CategoryService categoryService,
                          JsonService jsonService, MinIoService minIoService){
        this.productRepository = productRepository;
        this.pathRepository = pathRepository;
        this.categoryService = categoryService;
        this.jsonService = jsonService;
        this.minIoService = minIoService;
    }
    public Product findProductById(Long productId) {
        return productRepository.findProductById(productId);
    }
    public Product findProductByName(String name) {return productRepository.findProductByName(name);}

    public List<Product> allProducts() {
        return productRepository.findAll();
    }

    public boolean saveProduct(Product product) {

        if (productRepository.findByName(product.getName()).isEmpty()){
            productRepository.save(product);
            return true;
        }

        return false;

    }

    public boolean updateProduct(Product product, String name, Category category, Long cost, String source) {

        if (name != null){ product.setName(name);}
        if (category != null){product.setCategory(category);}
        if (cost != null) {product.setCost(cost);}
        if (source != null){product.setSource(source);}
        productRepository.saveAndFlush(product);

        return true;
    }
    public boolean deleteProduct(Long productId) {

        Optional<Product> productOptional = productRepository.findById(productId);

        if (productOptional.isPresent()) {

            Product product = productOptional.get();

            minIoService.deleteObject(product.getPhoto());

            productRepository.deleteById(productId);
            return true;
        }
        return false;
    }

    private boolean isCategory(String name){
        return categoryService.findCategoryByName(name) != null;
    }

    private String findCategory(List<String> input) {

        String paramCategory = null;

        for (String part : input) {
            if (isCategory(part)) {
                paramCategory = part;
                break;
            }
        }

        return paramCategory;
    }

    public List<Product> searchProducts(String input) {

        List<String> inputValues = new ArrayList<>(Arrays.asList(input.split("\\s+")));
        String category = findCategory(inputValues);
        if (category != null ) { inputValues.remove(category); }

        List<Product> productList = new ArrayList<>();

        if (category != null){

            if(inputValues.isEmpty()) {
                productList.addAll(productRepository.findByCategory(
                        categoryService.findCategoryByName(category)));

                return productList;
            }

            for (String inputValue : inputValues) {
                productList.addAll(productRepository.findByNameAndCategory(
                        inputValue,
                        categoryService.findCategoryByName(category)));
            }
        } else {

            if(inputValues.isEmpty()) {
                return productList;
            }

            for (String inputValue : inputValues) {
                productList.addAll(productRepository.findByName(inputValue));
            }
        }

        return productList;
    }

    public List<Product> productgtList(Long idMin) {
        return em.createQuery("SELECT u FROM Product u WHERE u.id > :paramId", Product.class)
                .setParameter("paramId", idMin).getResultList();
    }
}
