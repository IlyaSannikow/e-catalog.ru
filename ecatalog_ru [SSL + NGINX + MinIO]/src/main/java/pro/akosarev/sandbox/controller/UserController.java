package pro.akosarev.sandbox.controller;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pro.akosarev.sandbox.entity.User;
import pro.akosarev.sandbox.service.UserService;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket.name}")
    private String bucketName;

    @GetMapping("/user-info")
    @ResponseBody
    public Map<String, Object> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isAuthenticated = false;
        Set<String> roles = new HashSet<>();
        boolean haveProfileImage = false; // новое поле для хранения состояния изображения профиля

        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ANONYMOUS"))) {
            isAuthenticated = true;

            authentication.getAuthorities().forEach(authority -> roles.add(authority.getAuthority()));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("isAuthenticated", isAuthenticated);
        response.put("roles", roles);

        return response;
    }

    @PostMapping("/changePassword")
    public ResponseEntity<String> changePassword(@RequestParam("password") String newPassword) {

        System.out.println("Пароль изменён на: " + newPassword); // Для отладки
        return ResponseEntity.ok("Пароль успешно изменен");
    }

    @PostMapping("/register")
    public void register(@RequestBody User user) {
        // Регистрация нового пользователя
        userService.registerUser(user);
    }
}
