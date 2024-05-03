package pro.akosarev.sandbox.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import pro.akosarev.sandbox.entity.Category;
import pro.akosarev.sandbox.entity.Product;
import pro.akosarev.sandbox.service.CategoryService;
import pro.akosarev.sandbox.service.CommentService;
import pro.akosarev.sandbox.service.ProductService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private CommentService commentService;

    @Mock
    private Model model;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testShowCreateProductPage() throws Exception {
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("Category1");
        categoryNames.add("Category2");
        when(categoryService.findAllName()).thenReturn(categoryNames); // Заменяет взятие данных из репозитория

        String viewName = productController.showCreateProductPage(model);

        assertEquals("create-product", viewName);
        verify(model, times(1)).addAttribute(eq("productForm"), any());
        verify(model, times(1)).addAttribute(eq("categoryOptions"), eq(categoryNames));
    }

    @Test
    public void testProductList() {
        List<Product> products = new ArrayList<>();
        when(productService.allProducts()).thenReturn(products);

        String viewName = productController.productList(model);

        assertEquals("products", viewName);
        verify(model, times(1)).addAttribute(eq("commentForm"), any());
        verify(model, times(1)).addAttribute(eq("allProducts"), eq(products));
    }

    @Test
    public void testAddProduct_WithValidProduct() {
        // Товар добавлен
        Product productForm = new Product();
        productForm.setName("TestProduct");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        Category category = new Category();
        category.setId(1L); // Устанавливаем id для категории
        when(categoryService.findCategoryId(anyString())).thenReturn(category);
        when(productService.saveProduct(productForm)).thenReturn(true); // Успешное сохранение продукта

        String viewName = productController.addProduct(productForm, "TestCategory", bindingResult, model);

        assertEquals("redirect:/create-product", viewName);
        verify(productService, times(1)).saveProduct(productForm);
        verify(model, never()).addAttribute(eq("productNameError"), anyString());
    }

    @Test
    public void testAddProduct_WithInvalidProduct() {
        // Товар не добавлен
        Product productForm = new Product();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        Category category = new Category();
        category.setId(1L); // Устанавливаем корректный id для категории
        when(categoryService.findCategoryId(anyString())).thenReturn(category);

        String viewName = productController.addProduct(productForm, "TestCategory", bindingResult, model);

        assertEquals("create-product", viewName);
        verify(productService, never()).saveProduct(productForm);
        verify(model, never()).addAttribute(eq("productNameError"), anyString());
    }

    @Test
    public void testAddProduct_ProductAlreadyExists() {
        // Проверка с товаром при наличии других товаров
        Product productForm = new Product();
        productForm.setName("ExistingProduct");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        Category category = new Category();
        category.setId(1L); // Устанавливаем корректный id для категории
        when(categoryService.findCategoryId(anyString())).thenReturn(category);
        when(productService.saveProduct(productForm)).thenReturn(false);

        String viewName = productController.addProduct(productForm, "TestCategory", bindingResult, model);

        assertEquals("create-product", viewName);
        verify(model, times(1)).addAttribute(eq("productNameError"), eq("Товар с таким именем уже зарегистрирован"));
    }

    @Test
    void testActionProductDelete() {
        // Проверка на удаление объекта
        Model model = mock(Model.class);

        String result = productController.actionProduct(1L, "Test", "TestCategory", 100L, "TestSource", "delete", model);

        verify(productService).deleteProduct(1L);
    }

    @Test
    void testActionProductUpdate() {
        // Проверка на передачу данных при переадресации
        Model model = mock(Model.class);
        Product mockProduct = mock(Product.class);
        when(productService.findProductById(1L)).thenReturn(mockProduct);

        String result = productController.actionProduct(1L, "Test", "TestCategory", 100L, "TestSource", "update", model);

        verify(model).addAttribute("product", mockProduct);
    }

    @Test
    void testActionProductUpdate2() {
        // Проверка на обновление данных
        Model model = mock(Model.class);
        Product mockProduct = mock(Product.class);
        when(productService.findProductById(1L)).thenReturn(mockProduct);

        String result = productController.actionProduct(1L, "UpdatedName", "UpdatedCategory", 200L, "UpdatedSource", "update2", model);

        verify(productService).updateProduct(mockProduct, "UpdatedName", "UpdatedCategory", 200L, "UpdatedSource");
    }

    @Test
    void testGtProduct() {
        Long productId = 1L;
        List<Product> expectedProducts = new ArrayList<>();
        when(productService.productgtList(productId)).thenReturn(expectedProducts);
        Model model = mock(Model.class);

        String viewName = productController.gtProduct(productId, model);

        assertEquals("products", viewName);
        verify(model).addAttribute("allProducts", expectedProducts);
    }
}
