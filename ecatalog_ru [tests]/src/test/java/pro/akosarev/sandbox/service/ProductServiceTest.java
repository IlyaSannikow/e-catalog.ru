package pro.akosarev.sandbox.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import pro.akosarev.sandbox.entity.Product;
import pro.akosarev.sandbox.repository.ProductRepository;

import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
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
        // Проверка обновление товара
        Product product = new Product();
        String name = "Updated Product";
        String category = "Updated Category";
        Long cost = 100L;
        String source = "Updated Source";

        boolean result = productService.updateProduct(product, name, category, cost, source);

        assertTrue(result);
        assertEquals(name, product.getName());
        assertEquals(category, product.getCategory());
        assertEquals(cost, product.getCost());
        assertEquals(source, product.getSource());
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
}
