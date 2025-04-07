package pro.akosarev.sandbox.controller;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import pro.akosarev.sandbox.entity.User;
import pro.akosarev.sandbox.repository.UserInfoRepository;
import pro.akosarev.sandbox.service.UserService;

import java.io.IOException;
import java.security.Principal;

@Controller
public class MinIOController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket.name}")
    private String bucketName;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            Principal principal,
            @RequestHeader("X-XSRF-TOKEN") String csrfToken) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a file to upload.");
            }

            String username = principal.getName();
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf('.')) : "";

            String objectName = "profile_image_" + username + extension;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            User user = userService.findByUsername(username);
            user.setHaveProfileImage(true);
            userService.updateUserInfo(user, objectName);

            return ResponseEntity.ok().body("File uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to upload file: " + e.getMessage());
        }
    }

    @GetMapping("/comic/share")
    public String getComicShareLink(@RequestParam String objectName, Model model) {
        String shareableLink = getShareableLink(objectName);
        model.addAttribute("shareableLink", shareableLink);
        return "comic_view"; // имя Thymeleaf шаблона для отображения комикса
    }

    public String getShareableLink(String objectName) {
        try {
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(60 * 60 * 24) // 1 день
                    .build();

            return minioClient.getPresignedObjectUrl(args);
        } catch (Exception e) {
            System.out.println("Error generating shareable link: " + e.getMessage());
            return null;
        }
    }
}
