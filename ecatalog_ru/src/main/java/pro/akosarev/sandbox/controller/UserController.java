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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pro.akosarev.sandbox.entity.User;
import pro.akosarev.sandbox.service.PasswordChangeRequest;
import pro.akosarev.sandbox.service.RecaptchaService;
import pro.akosarev.sandbox.service.UserService;

import java.security.Principal;
import java.util.*;

@Controller
public class UserController {

    private UserService userService;

    private MinioClient minioClient;

    private RecaptchaService recaptchaService;

    @Value("${minio.bucket.name}")
    private String bucketName;

    public UserController(UserService userService, MinioClient minioClient, RecaptchaService recaptchaService) {
        this.userService = userService;
        this.minioClient = minioClient;
        this.recaptchaService = recaptchaService;
    }

    @GetMapping("/")
    public String home(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("isAuthenticated", auth != null && auth.isAuthenticated());
        model.addAttribute("isAdmin", auth != null && auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN")));
        return "index";
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
    public ResponseEntity<?> register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam("g-recaptcha-response") String recaptchaResponse,
            @RequestHeader(name = "X-XSRF-TOKEN") String csrfToken
    ) {
        if (!recaptchaService.validateRecaptcha(recaptchaResponse)) {
            throw new SecurityException("Неверная CAPTCHA");
        }

        try {
            userService.registerUser(username, password, recaptchaResponse);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Ошибка сервера"));
        }
    }
}
