package pro.akosarev.sandbox.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pro.akosarev.sandbox.entity.Category;
import pro.akosarev.sandbox.entity.Product;
import pro.akosarev.sandbox.repository.ProductRepository;

import java.util.List;
import java.util.Optional;
import java.util.function.DoubleUnaryOperator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    public ProductServiceTest() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testFindProductById() {
        // Поиск по id
        Long productId = 1L;
        Product expectedProduct = new Product();
        when(productRepository.findById(productId)).thenReturn(Optional.of(expectedProduct));

        Product actualProduct = productService.findProductById(productId);

        assertNotNull(actualProduct);
        assertEquals(expectedProduct, actualProduct);
    }

    @Test
    void testAllProducts() {
        // Получение списка товаров
        List<Product> expectedProducts = List.of(new Product(), new Product());
        when(productRepository.findAll()).thenReturn(expectedProducts);

        List<Product> actualProducts = productService.allProducts();

        assertNotNull(actualProducts);
        assertEquals(expectedProducts.size(), actualProducts.size());
    }

    @Test
    void testSaveProduct() {
        // Проверка сохранения товара в бд
        Product product = new Product();
        product.setName("Test Product");
        when(productRepository.findByName(product.getName())).thenReturn(null);

        boolean result = productService.saveProduct(product);

        assertTrue(result);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void testUpdateProduct() {
        Product product = new Product();
        product.setName("Old Name");
        Category category = new Category();
        category.setId(1L);
        product.setCategory(category);
        product.setCost(100L);
        product.setSource("Old Source");

        String newName = "New Name";
        Category newCategory = new Category();
        newCategory.setId(2L);
        Long newCost = 200L;
        String newSource = "New Source";

        when(productRepository.saveAndFlush(any(Product.class))).thenReturn(product);

        boolean result = productService.updateProduct(product, newName, newCategory, newCost, newSource);

        assertTrue(result);
        assertEquals(newName, product.getName());
        assertEquals(newCategory, product.getCategory());
        assertEquals(newCost, product.getCost());
        assertEquals(newSource, product.getSource());
        verify(productRepository, times(1)).saveAndFlush(product);
    }

    @Test
    void testDeleteProduct() {
        // Проверка удаления товара
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.of(new Product()));

        boolean result = productService.deleteProduct(productId);

        assertTrue(result);
        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    void testDeleteProductWhenProductNotFound() {
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        boolean result = productService.deleteProduct(productId);

        assertFalse(result);
        verify(productRepository, never()).deleteById(productId);
    }

    @Test
    public void testForTestProduct_FirstDerivative() {
        DoubleUnaryOperator sinFunction = Math::sin;
        double x = 45.0;
        double h = 0.001;
        ProductService service = new ProductService();
        boolean result = service.forTestProduct(x, h, sinFunction);
        assertFalse(result);
    }

    @Test
    public void testForTestProduct_SecondDerivative() {
        DoubleUnaryOperator sinFunction = Math::sin;
        double x = 45.0;
        double h = 0.001;
        ProductService service = new ProductService();
        boolean result = service.forTestProduct(x, h, sinFunction);
        assertFalse(result);
    }

    @Test
    public void testNaNOrder() {
        // Проверка на NaN при недопустимом значении order
        DoubleUnaryOperator identityFunction = x -> x;
        double x = 30;
        double h = 0.00001;
        double actual = ProductService.derivative(identityFunction, x, h, 3);
        assertTrue(Double.isNaN(actual), "Ожидается NaN при недопустимом значении order");
    }
}
