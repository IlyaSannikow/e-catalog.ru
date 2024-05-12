package pro.akosarev.sandbox.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pro.akosarev.sandbox.entity.Product;
import pro.akosarev.sandbox.service.CompareService;
import pro.akosarev.sandbox.service.PathService;
import pro.akosarev.sandbox.service.UserService;

import java.security.Principal;
import java.util.List;

@Controller
public class CompareController {

    CompareService compareService;
    UserService userService;

    PathService pathService;

    public CompareController(CompareService compareService, UserService userService, PathService pathService){
        this.compareService = compareService;
        this.userService = userService;
        this.pathService = pathService;
    }

    @GetMapping("/compare-products")
    public String comparePage(Principal principal, Model model) throws JsonProcessingException {

        String userId = principal.getName();

        model.addAttribute("compareProducts", compareService.getProductsByUser(userService.findUserById(userId)));
        model.addAttribute("path", pathService.findPath("image"));

        return "compare-products";
    }

    @PostMapping("/deleteCompare")
    public String deleteCompare(@RequestParam(required = true, defaultValue = "" ) String productName,
                                Principal principal){

        compareService.deleteCompare(principal.getName(), productName);

        return "redirect:/compare-products";
    }
}
