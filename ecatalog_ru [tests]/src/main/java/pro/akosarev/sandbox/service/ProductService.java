package pro.akosarev.sandbox.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import pro.akosarev.sandbox.entity.Category;
import pro.akosarev.sandbox.entity.Product;
import pro.akosarev.sandbox.repository.ProductRepository;

import java.util.*;
import java.util.function.DoubleUnaryOperator;

@Service
public class ProductService  {
    @PersistenceContext
    private EntityManager em;
    ProductRepository productRepository;
    public ProductService(){
        this.productRepository = productRepository;
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

        if (productRepository.findById(productId).isPresent()) {
            productRepository.deleteById(productId);
            return true;
        }
        return false;
    }

    public boolean forTestProduct(double x, double h, DoubleUnaryOperator sinFunction){
        // Вычисление первой производной
        double firstDerivative = derivative(sinFunction, x, h, 1);
        System.out.println("f'(x) = " + firstDerivative);

        // Вычисление второй производной
        double secondDerivative = derivative(sinFunction, x, h, 2);
        System.out.println("f''(x) = " + secondDerivative);

        return false;
    }

    static double derivative(DoubleUnaryOperator function, double x, double h, int order) {
        if (order == 1) {
            return (function.applyAsDouble(Math.toRadians(x) + h) - function.applyAsDouble(Math.toRadians(x))) / h;
        } else if (order == 2) {
            return (function.applyAsDouble(Math.toRadians(x) + h) - 2 * function.applyAsDouble(Math.toRadians(x)) + function.applyAsDouble(Math.toRadians(x) - h)) / (h * h);
        }

        return Double.NaN;
    }

    public List<Product> searchProducts(String input) {
        List<String> inputValues = Arrays.asList(input.split("\\s+"));
        Set<List<String>> permutations = generatePermutations(inputValues);
        Set<Product> uniqueProducts = new HashSet<>();
        for (List<String> permutation : permutations) {
            String param1 = permutation.get(0);
            String param2 = permutation.size() > 1 ? permutation.get(1) : "";
            List<Product> foundProducts = new ArrayList<>();
            if (param2.isEmpty()) {
                foundProducts.addAll(productRepository.findByNameContainingIgnoreCase(param1));
                foundProducts.addAll(productRepository.findByCategoryNameContainingIgnoreCase(param1));
            } else {
                foundProducts.addAll(productRepository.findByNameContainingIgnoreCaseAndCategoryNameContainingIgnoreCase(param1, param2));
            }
            uniqueProducts.addAll(foundProducts);
        }
        return new ArrayList<>(uniqueProducts);
    }

    private Set<List<String>> generatePermutations(List<String> inputValues) {
        Set<List<String>> permutations = new HashSet<>();
        permute(inputValues, 0, permutations);
        return permutations;
    }

    private void permute(List<String> arr, int k, Set<List<String>> permutations) {
        for (int i = k; i < arr.size(); i++) {
            Collections.swap(arr, i, k);
            permute(arr, k + 1, permutations);
            Collections.swap(arr, k, i);
        }
        if (k == arr.size() - 1) {
            permutations.add(new ArrayList<>(arr));
        }
    }

    public List<Product> productgtList(Long idMin) {
        return em.createQuery("SELECT u FROM Product u WHERE u.id > :paramId", Product.class)
                .setParameter("paramId", idMin).getResultList();
    }
}
