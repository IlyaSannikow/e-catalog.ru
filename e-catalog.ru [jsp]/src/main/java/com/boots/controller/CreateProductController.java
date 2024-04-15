package com.boots.controller;

import com.boots.entity.Category;
import com.boots.entity.Product;
import com.boots.entity.User;
import com.boots.service.CategoryService;
import com.boots.service.ProductService;
import com.boots.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class CreateProductController {

    ProductService productService;
    CategoryService categoryService;

    public CreateProductController(ProductService productService, CategoryService categoryService){
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/createProduct")
    public String registration(Model model) throws Exception {
        model.addAttribute("productForm", new Product()); // Ключ, Значение
        model.addAttribute("categoryOptions", categoryService.findAllName());
        return "createProduct";
    }

    @PostMapping("/createProduct")
    public String addProduct(@ModelAttribute("productForm") @Valid Product productForm,
                             @RequestParam(required = true, defaultValue = "" ) String category,
                             BindingResult bindingResult,
                             Model model) {

        Category categoryId = categoryService.findCategoryId(category);
        productForm.setCategory(categoryId.getId().toString());

        if (bindingResult.hasErrors()) {
            return "createProduct";
        }
        if (!productService.saveProduct(productForm)){
            model.addAttribute("productNameError", "Товар с таким именем уже зарегестрирован");
            return "createProduct";
        }

        return "redirect:/";
    }
}