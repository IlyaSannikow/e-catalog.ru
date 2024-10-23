package pro.akosarev.sandbox.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.GrantedAuthority;

import java.util.stream.Collectors;

@Controller
public class RedirectController {

    @GetMapping({"/profile.html", "/index.html", "/login_account"})
    public String home(Model model, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = (authentication != null && authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken));

        model.addAttribute("isAuthenticated", isAuthenticated);

        if (isAuthenticated) {
            var authorities = authentication.getAuthorities();
            model.addAttribute("roles", authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
        }

        // Определяем, какое представление возвращать на основе текущего URL
        String requestUri = request.getRequestURI();

        if (requestUri.equals("/profile.html")) {
            return "profile"; // Возвращаем представление для профиля
        } else {
            return "index"; // Возвращаем представление для главной страницы
        }
    }
}
