package pro.akosarev.sandbox.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

@Controller
public class RedirectController {
    @GetMapping("admin")
    public String getManagerPage() {
        return "admin";
    }

    @GetMapping("registration")
    public String getRegistrationPage() {
        return "registration";
    }

    @GetMapping("/index.html")
    public String index(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isAuthenticated = false;

        if (authentication != null) {
            isAuthenticated = authentication.isAuthenticated() &&
                    !authentication.getAuthorities().stream()
                            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ANONYMOUS"));

            model.addAttribute("isAuthenticated", isAuthenticated);

            Set<String> roles = new HashSet<>();
            if (authentication != null) {
                authentication.getAuthorities().forEach(authority -> roles.add(authority.getAuthority()));
            }
            model.addAttribute("roles", roles);
        } else {
            System.out.println("Authentication is null");
        }

        return "index"; // Имя вашего шаблона
    }

}
