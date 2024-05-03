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

@SpringBootTest
public class CategoryServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    public void testFindCategoryById_WhenValidId_ReturnsCategory() {
        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        Category result = categoryService.findCategoryById(categoryId);

        assertEquals(category, result);
    }

    @Test
    public void testFindCategoryById_WhenInvalidId_ReturnsEmptyCategory() {
        Long categoryId = 1L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        Category result = categoryService.findCategoryById(categoryId);

        assertEquals(new Category(), result);
    }

    @Test
    public void testAllCategories_ReturnsListOfCategories() {
        List<Category> categories = Arrays.asList(new Category(), new Category());
        when(categoryRepository.findAll()).thenReturn(categories);

        List<Category> result = categoryService.allCategories();

        assertEquals(categories, result);
    }

    @Test
    public void testFindAllName_ReturnsListOfNames() {
        List<String> names = Arrays.asList("Name1", "Name2");
        when(categoryRepository.findAllCategories()).thenReturn(names);

        List<String> result = categoryService.findAllName();

        assertEquals(names, result);
    }

    @Test
    public void testFindAllId_ReturnsListOfIds() {
        List<Long> ids = Arrays.asList(1L, 2L);
        when(categoryRepository.findAllId()).thenReturn(ids);

        List<Long> result = categoryService.findAllId();

        assertEquals(ids, result);
    }

    @Test
    public void testFindCategoryId_ReturnsCategory() {
        String categoryName = "Test";
        Category category = new Category();
        category.setName(categoryName);
        when(categoryRepository.findByName(categoryName)).thenReturn(category);

        Category result = categoryService.findCategoryId(categoryName);

        assertEquals(category, result);
    }

    @Test
    public void testSaveCategory_WhenCategoryNotExists_ReturnsTrue() {
        Category category = new Category();
        category.setId(1L);
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.empty());

        boolean result = categoryService.saveCategory(category);

        assertTrue(result);
    }

    @Test
    public void testSaveCategory_WhenCategoryExists_ReturnsFalse() {
        Category category = new Category();
        category.setId(1L);
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));

        boolean result = categoryService.saveCategory(category);

        assertFalse(result);
    }

    @Test
    public void testUpdateCategory_ReturnsTrue() {
        Category category = new Category();
        String newCategoryName = "New Name";

        boolean result = categoryService.updateCategory(category, newCategoryName);

        assertTrue(result);
        assertEquals(newCategoryName, category.getName());
    }

    @Test
    public void testDeleteCategory_WhenCategoryExists_ReturnsTrue() {
        Long categoryId = 1L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(new Category()));

        boolean result = categoryService.deleteCategory(categoryId);

        assertTrue(result);
    }

    @Test
    public void testDeleteCategory_WhenCategoryNotExists_ReturnsFalse() {
        Long categoryId = 1L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        boolean result = categoryService.deleteCategory(categoryId);

        assertFalse(result);
    }
}
