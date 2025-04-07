package pro.akosarev.sandbox.controller;

import io.minio.MinioClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pro.akosarev.sandbox.entity.User;
import pro.akosarev.sandbox.service.PasswordChangeRequest;
import pro.akosarev.sandbox.service.UserService;

import java.security.Principal;
import java.util.*;

@Controller
public class UserController {

    private UserService userService;

    private MinioClient minioClient;

    @Value("${minio.bucket.name}")
    private String bucketName;

    public UserController(UserService userService, MinioClient minioClient) {
        this.userService = userService;
        this.minioClient = minioClient;
    }

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
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody PasswordChangeRequest request,
            Principal principal) {

        Map<String, String> response = new HashMap<>();

        try {
            userService.changePassword(
                    principal.getName(),
                    request.getCurrentPassword(),
                    request.getNewPassword()
            );

            response.put("message", "Пароль успешно изменен");
            return ResponseEntity.ok(response);
        } catch (UsernameNotFoundException e) {
            response.put("message", "Пользователь не найден");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (SecurityException e) {
            response.put("message", "Текущий пароль неверен");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("message", "Ошибка при изменении пароля");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/register")
    public void register(@RequestBody User user) {
        // Регистрация нового пользователя
        userService.registerUser(user);
    }
}
