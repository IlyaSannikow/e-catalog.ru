package pro.akosarev.sandbox.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import pro.akosarev.sandbox.entity.Category;
import pro.akosarev.sandbox.repository.CategoryRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class CategoryServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    public void testForTestCategory_1x1() {
        int[][] data = {{5}};
        CategoryService service = new CategoryService();
        boolean result = service.forTestCategory(data);
        assertFalse(result);
    }

    @Test
    public void testForTestCategory_2x2() {
        int[][] data = {
                {1, 2},
                {3, 4}
        };
        CategoryService service = new CategoryService();
        boolean result = service.forTestCategory(data);
        assertFalse(result);
    }

    @Test
    public void testForTestCategory_3x3() {
        int[][] data = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        CategoryService service = new CategoryService();
        boolean result = service.forTestCategory(data);
        assertFalse(result);
    }
}
