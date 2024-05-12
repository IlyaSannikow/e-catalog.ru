package pro.akosarev.sandbox.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import net.minidev.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import pro.akosarev.sandbox.entity.*;
import pro.akosarev.sandbox.service.*;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Controller
public class ProductController {
    ProductService productService;
    CommentService commentService;
    CategoryService categoryService;
    ImageService imageService;
    PathService pathService;
    UserService userService;
    CompareService compareService;
    JsonService jsonService;

    public ProductController(ProductService productService, CategoryService categoryService, CommentService commentService,
                             ImageService imageService, PathService pathService, UserService userService, CompareService compareService,
                             JsonService jsonService){
        this.productService = productService;
        this.categoryService = categoryService;
        this.commentService = commentService;
        this.imageService = imageService;
        this.pathService = pathService;
        this.userService = userService;
        this.compareService = compareService;
        this.jsonService = jsonService;
    }

    @GetMapping("/create-product")
    public String showCreateProductPage(Model model) throws Exception{
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

        model.addAttribute("allProducts", products);
        model.addAttribute("path", pathService.findPath("image"));

        session.setAttribute("searchProduct", products);

        return "products";
    }

    @PostMapping("/create-product")
    public String addProduct(@ModelAttribute("productForm") @Valid Product productForm,
                             @RequestParam("image") MultipartFile imageFile,
                             BindingResult bindingResult,
                             Model model) throws IOException{

        if (bindingResult.hasErrors()) {
            return "create-product";
        }

        String pathToImage =  imageService.saveImageFolder(imageFile);
        productForm.setInEcatalogDB(true);
        productForm.setPhoto(pathToImage);

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

        HttpSession session = request.getSession();
        List<Product> products = (List<Product>) session.getAttribute("searchProduct");

        for (Product prod : products) {
            if (prod.getName().equals(productName)) {
                compareService.saveCompare(user, productName);
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
