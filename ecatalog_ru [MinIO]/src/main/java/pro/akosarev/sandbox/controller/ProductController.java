package pro.akosarev.sandbox.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pro.akosarev.sandbox.entity.*;
import pro.akosarev.sandbox.service.*;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Controller
public class ProductController {
    ProductService productService;
    CommentService commentService;
    CategoryService categoryService;
    MinIoService minIoService;
    PathService pathService;
    UserService userService;
    CompareService compareService;
    JsonService jsonService;
    SynchronizationService syncService;

    public ProductController(ProductService productService, CategoryService categoryService, CommentService commentService,
                             MinIoService minIoService, PathService pathService, UserService userService, CompareService compareService,
                             JsonService jsonService, SynchronizationService syncService){
        this.productService = productService;
        this.categoryService = categoryService;
        this.commentService = commentService;
        this.pathService = pathService;
        this.userService = userService;
        this.compareService = compareService;
        this.jsonService = jsonService;
        this.minIoService = minIoService;
        this.syncService = syncService;
    }

    @GetMapping("/create-product")
    public String showCreateProductPage(Model model) {
        model.addAttribute("productForm", new Product()); // Ключ, Значение
        model.addAttribute("categoryOptions", categoryService.allCategories());
        return "create-product";
    }

    @GetMapping("/products")
    public String productList(HttpServletRequest request, Model model) throws JsonProcessingException {
        model.addAttribute("commentForm", new Comment());

        HttpSession session = request.getSession();
        String searchInput = (String) session.getAttribute("searchInput");
        List<Product> products = productService.searchProducts(searchInput);

        session.setAttribute("searchProduct", products);

        model.addAttribute("allProducts", products);

        return "products";
    }

    @GetMapping("/display-image/{productId}")
    public void displayImage(@PathVariable String productId, HttpServletResponse response) {

        InputStream imageStream = minIoService.getObject(productId);

        if (imageStream != null) {
            response.setContentType("image/png");

            try (InputStream in = imageStream; OutputStream out = response.getOutputStream()) {
                IOUtils.copy(in, out);
            } catch (IOException e) {
                System.out.println("Error streaming image: " + e.getMessage());
            }
        } else {
            System.out.println("Image not found for product ID: " + productId);
        }
    }

    @PostMapping("/create-product")
    public String addProduct(@ModelAttribute("productForm") @Valid Product productForm,
                             @ModelAttribute File request,
                             BindingResult bindingResult,
                             Model model) {

        if (bindingResult.hasErrors()) {
            return "create-product";
        }

        File image = minIoService.uploadFile(request);
        productForm.setPhoto(image.getFilename());
        productForm.setExternalId("None");

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

    @PostMapping("/productCompare")
    public String addProductToUser(@RequestParam(required = true, defaultValue = "" ) String productName,
                                   HttpServletRequest request,
                                   Principal principal) {

        String userId = principal.getName();
        User user = userService.findUserById(userId);
        Product product = productService.findProductByName(productName);

        HttpSession session = request.getSession();
        List<Product> products = (List<Product>) session.getAttribute("searchProduct");

        for (Product prod : products) {
            if (prod.getName().equals(productName)) {
                compareService.saveCompare(user, product);
                break;
            }
        }

        return "redirect:/products";
    }

    @GetMapping("/products/gt/{productId}")
    public String gtProduct(@PathVariable("productId") Long productId, Model model) {
        model.addAttribute("allProducts", productService.productgtList(productId));
        return "products";
    }

}
