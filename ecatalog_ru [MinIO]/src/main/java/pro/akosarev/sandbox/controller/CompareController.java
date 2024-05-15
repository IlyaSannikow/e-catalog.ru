package pro.akosarev.sandbox.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pro.akosarev.sandbox.service.CompareService;
import pro.akosarev.sandbox.service.PathService;
import pro.akosarev.sandbox.service.UserService;

import java.security.Principal;

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
    public String comparePage(Principal principal, Model model) {

        String userId = principal.getName();

        model.addAttribute("compareProducts", compareService.getProductsByUser(userService.findUserById(userId)));

        return "compare-products";
    }

    @PostMapping("/deleteCompare")
    public String deleteCompare(@RequestParam(required = true, defaultValue = "" ) String productName,
                                Principal principal){

        compareService.deleteCompare(principal.getName(), productName);

        return "redirect:/compare-products";
    }
}
