package com.boots.controller;

import com.boots.entity.Comment;
import com.boots.entity.Product;
import com.boots.entity.User;
import com.boots.repository.ProductRepository;
import com.boots.service.CommentService;
import com.boots.service.ProductService;
import com.boots.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ProductController {
    ProductService productService;
    CommentService commentService;

    UserService userService;


    public ProductController(ProductService productService, CommentService commentService, UserService userService){
        this.userService = userService;
        this.productService = productService;
        this.commentService = commentService;
    }
    @GetMapping("/productList")
    public String productList(Model model) {
        model.addAttribute("allProducts", productService.allProducts()); // Ключ, Значение
        return "productList";
    }

    @GetMapping("/productList/{id}")
    public String productInfo(@PathVariable("id") Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        model.addAttribute("allComments", commentService.allComments());
        model.addAttribute("product", productService.findProductById(id));
        model.addAttribute("user", userService.findUserByUsername(authentication.getName()));
        return "product-info";
    }

    @PostMapping("/productList")
    public String  deleteProduct(@RequestParam(required = true, defaultValue = "" ) Long productId,
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

        return "redirect:/productList";
    }
    @GetMapping("/productList/gt/{productId}")
    public String gtProduct(@PathVariable("productId") Long productId, Model model) {
        model.addAttribute("allProducts", productService.productgtList(productId));
        return "productList";
    }
}
