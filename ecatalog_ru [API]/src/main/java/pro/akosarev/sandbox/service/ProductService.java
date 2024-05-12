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

    JsonService jsonService;
    public ProductService(ProductRepository productRepository, PathRepository pathRepository, CategoryService categoryService,
                          JsonService jsonService){
        this.productRepository = productRepository;
        this.pathRepository = pathRepository;
        this.categoryService = categoryService;
        this.jsonService = jsonService;
    }
    public Product findProductById(Long productId) {
        Optional<Product> productFromDb = productRepository.findById(productId);
        return productFromDb.orElse(new Product());
    }

    public List<Product> allProducts() {
        return productRepository.findAll();
    }

    public boolean saveProduct(Product product) {
        Product productFromDB = productRepository.findByName(product.getName());

        if (productFromDB != null) {
            return false;
        }

        productRepository.save(product);

        return true;
    }

    public boolean updateProduct(Product product, String name, Category category, Long cost, String source) {

        product.setName(name);
        product.setCategory(category);
        product.setCost(cost);
        product.setSource(source);
        productRepository.saveAndFlush(product);

        return true;
    }
    public boolean deleteProduct(Long productId) {

        Optional<Product> productOptional = productRepository.findById(productId);

        if (productOptional.isPresent()) {

            String pathImage = pathRepository.findByName("fullImage").getPath();

            Product product = productOptional.get();
            String photo = product.getPhoto();

            deletePhoto("." + pathImage + photo);

            productRepository.deleteById(productId);
            return true;
        }
        return false;
    }

    private boolean deletePhoto(String photoPath) {
        File file = new File(photoPath);
        return file.delete();
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

    public List<Product> searchProducts(String input) throws JsonProcessingException {

        List<Path> shopPaths = jsonService.getAPIPaths();
        List<String> inputValues = new ArrayList<>(Arrays.asList(input.split("\\s+")));
        String category = findCategory(inputValues);
        if (category != null ) { inputValues.remove(category); }


        List<Product> foundProducts = new ArrayList<>();
        List<JSON> foundJsonProducts = new ArrayList<>();

        List<Product> productList = new ArrayList<>();

        if (category != null){

            foundProducts.addAll(productRepository.findByCategoryNameContainingIgnoreCase(category));

            for(Path shop: shopPaths){
                List<JSON> jsonProducts = jsonService.getJSON(shop);
                foundJsonProducts.addAll(jsonService.getJsonProduct(jsonProducts ,shop.getSource(), category));
            }

            foundProducts.addAll(jsonService.convertJsonToProduct(foundJsonProducts));

            if (!inputValues.isEmpty()) {
                for (int i = 0; i < inputValues.size(); i++) {
                    for (Product product : foundProducts) {

                        if (Objects.equals(product.getName(), inputValues.get(i))) {
                            productList.add(product);
                        }
                    }
                }
            } else { return foundProducts; }
        } else {

            if (!inputValues.isEmpty()) {
                for (int i = 0; i < inputValues.size(); i++) {
                    productList.addAll(productRepository.findByNameContainingIgnoreCase(inputValues.get(i)));

                    for(Path shop: shopPaths){
                        List<JSON> jsonProducts = jsonService.getJSON(shop);
                        foundJsonProducts.addAll(jsonService.getJsonProduct(jsonProducts ,shop.getSource(), inputValues.get(i)));
                    }
                }

                productList.addAll(jsonService.convertJsonToProduct(foundJsonProducts));

            } else { return foundProducts; }
        }
        return productList;
    }

    public List<Product> productgtList(Long idMin) {
        return em.createQuery("SELECT u FROM Product u WHERE u.id > :paramId", Product.class)
                .setParameter("paramId", idMin).getResultList();
    }
}
