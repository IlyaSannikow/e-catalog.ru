package pro.akosarev.sandbox.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pro.akosarev.sandbox.entity.Category;
import pro.akosarev.sandbox.entity.Comment;
import pro.akosarev.sandbox.entity.Product;
import pro.akosarev.sandbox.service.CategoryService;
import pro.akosarev.sandbox.service.CommentService;
import pro.akosarev.sandbox.service.ProductService;

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
        model.addAttribute("categoryOptions", categoryService.findAllName());
        return "create-product";
    }

    @GetMapping("/products")
    public String productList(Model model) {
        model.addAttribute("commentForm", new Comment());
        model.addAttribute("allProducts", productService.allProducts()); // Ключ, Значение
        return "products";
    }

    @PostMapping("/create-product")
    public String addProduct(@ModelAttribute("productForm") @Valid Product productForm,
                             @RequestParam(required = true, defaultValue = "" ) String category,
                             BindingResult bindingResult,
                             Model model) {

        Category categoryId = categoryService.findCategoryId(category);
        productForm.setCategory(categoryId.getId().toString());

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
                                 @RequestParam(required = true, defaultValue = "" ) String category,
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
            return "/product-update";
        }
        if (action.equals("update2")){
            Product product = productService.findProductById(productId);
            productService.updateProduct(product, name, category, cost, source);
        }

        return "redirect:/products";
    }
    @GetMapping("/products/gt/{productId}")
    public String gtProduct(@PathVariable("productId") Long productId, Model model) {
        model.addAttribute("allProducts", productService.productgtList(productId));
        return "products";
    }

}
