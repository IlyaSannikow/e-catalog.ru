package pro.akosarev.sandbox.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pro.akosarev.sandbox.entity.Category;
import pro.akosarev.sandbox.entity.Comment;
import pro.akosarev.sandbox.entity.Product;
import pro.akosarev.sandbox.service.CategoryService;
import pro.akosarev.sandbox.service.CommentService;
import pro.akosarev.sandbox.service.ProductService;

import java.util.List;

@Controller
public class ProductController {

    ProductService productService;
    CommentService commentService;
    CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService, CommentService commentService){
        this.productService = productService;
        this.categoryService = categoryService;
        this.commentService = commentService;
    }

    @GetMapping("/create-product")
    public String showCreateProductPage(Model model) throws Exception{
        model.addAttribute("productForm", new Product()); // Ключ, Значение
        model.addAttribute("categoryOptions", categoryService.allCategories());
        return "create-product";
    }

    @GetMapping("/products")
    public String productList(HttpServletRequest request, Model model) {
        model.addAttribute("commentForm", new Comment());

        HttpSession session = request.getSession();
        String searchInput = (String) session.getAttribute("searchInput");
        List<Product> products = productService.searchProducts(searchInput);

        model.addAttribute("allProducts", products);

        return "products";
    }

    @PostMapping("/create-product")
    public String addProduct(@ModelAttribute("productForm") @Valid Product productForm,
                             BindingResult bindingResult,
                             Model model) {

        if (bindingResult.hasErrors()) {
            return "create-product";
        }

        if (!productService.saveProduct(productForm)){
            model.addAttribute("productNameError", "Товар с таким именем уже зарегистрирован");
            return "create-product";
        }

        return "redirect:/create-product";
    }

    @PostMapping("/products")
    public String actionProduct(@RequestParam(required = true, defaultValue = "" ) Long productId,
                                 @RequestParam(required = true, defaultValue = "" ) String name,
                                 @RequestParam(required = true, defaultValue = "" ) Long category,
                                 @RequestParam(required = true, defaultValue = "" ) Long cost,
                                 @RequestParam(required = true, defaultValue = "" ) String source,
                                 @RequestParam(required = true, defaultValue = "" ) String action,
                                 Model model) {


        if (action.equals("delete")){
            productService.deleteProduct(productId);
        }
        if (action.equals("update")){
            Product product = productService.findProductById(productId);
            model.addAttribute("product", product);
            model.addAttribute("categoryOptions", categoryService.allCategories());
            return "/product-update";
        }
        if (action.equals("update2")){
            Product product = productService.findProductById(productId);
            Category categoryObj = categoryService.findCategoryById(category); // Найти категорию по имени
            productService.updateProduct(product, name, categoryObj, cost, source);
        }

        return "redirect:/products";
    }

    @PostMapping("/search")
    public String searchProducts(@RequestParam(value = "input", required = false) String input,
                                 HttpServletRequest request) {

        HttpSession session = request.getSession();
        session.setAttribute("searchInput", input);

        return "redirect:/products";
    }

    @GetMapping("/products/gt/{productId}")
    public String gtProduct(@PathVariable("productId") Long productId, Model model) {
        model.addAttribute("allProducts", productService.productgtList(productId));
        return "products";
    }

}
