package pro.akosarev.sandbox.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import java.security.Principal;

@Controller
public class AdminController {

    @GetMapping("/admin")
    public String list(Principal principal, Authentication authentication , Model model) {

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String username = oidcUser.getPreferredUsername(); // Получение имени пользователя

        model.addAttribute("username",username);
        return "admin";
    }
}