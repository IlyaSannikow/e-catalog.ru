package pro.akosarev.sandbox.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import pro.akosarev.sandbox.entity.Category;
import pro.akosarev.sandbox.service.CategoryService;

@Controller
public class IndexController {

    CategoryService categoryService;

    public IndexController(CategoryService categoryService){
        this.categoryService = categoryService;
    }

    @GetMapping("/index.html")
    public String mainPage(Model model){

        model.addAttribute("categoryOptions", categoryService.allCategories());
        return "/index";
    }

    @PostMapping("/search")
    public String searchProducts(@RequestParam(value = "input", required = false) String input,
                                 HttpServletRequest request) {

        HttpSession session = request.getSession();
        session.setAttribute("searchInput", input);

        return "redirect:/products";
    }

    @PostMapping("/categorySearch")
    public String searchProductCategory(@RequestParam(value = "category", required = false) String category,
                                        HttpServletRequest request) {

        HttpSession session = request.getSession();
        session.setAttribute("searchInput", category);

        return "redirect:/products";
    }
}
