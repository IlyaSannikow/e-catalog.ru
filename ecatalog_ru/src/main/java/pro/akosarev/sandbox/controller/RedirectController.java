package pro.akosarev.sandbox.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import pro.akosarev.sandbox.entity.User;
import pro.akosarev.sandbox.entity.UserInfo;
import pro.akosarev.sandbox.service.UserService;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Controller
public class RedirectController {

    @Autowired
    private MinIOController minIOController;
    @Autowired
    private UserService userService;

    @GetMapping("admin")
    public String getManagerPage() {
        return "admin";
    }

    @GetMapping("registration")
    public String getRegistrationPage() {
        return "registration";
    }

    @GetMapping("/profile")
    public String getProfilePage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean haveProfileImage = false; // новое поле для хранения состояния изображения профиля
        String shareableLink = null; // Для хранения временной ссылки на изображение

        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ANONYMOUS"))) {

            // Получаем текущее имя пользователя
            String username = authentication.getName();

            // Предполагаем, что у вас есть метод для получения пользователя по имени
            User user = userService.findByUsername(username);
            if (user != null) {
                haveProfileImage = user.isHaveProfileImage(); // Проверяем наличие профильного изображения
                UserInfo userInfo = user.getUserInfo();

                if (userInfo != null) {
                    String objectName = userInfo.getUrlImage(); // Получаем название изображения

                    // Генерация временной ссылки
                    shareableLink = minIOController.getShareableLink(objectName);
                }
            }
        }

        // Добавляем значение в модель
        model.addAttribute("haveProfileImage", haveProfileImage);
        model.addAttribute("shareableLink", shareableLink);

        return "profile"; // Возвращаем страницу профиля
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/logout")
    public String showLogoutPage(Model model, HttpServletRequest request) {
        return "logout";
    }

}
