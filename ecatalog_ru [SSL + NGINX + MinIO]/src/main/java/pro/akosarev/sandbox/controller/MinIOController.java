package pro.akosarev.sandbox.controller;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
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
    public String uploadFile(Model model, MultipartFile file, Principal principal) {
        try {
            if (file.isEmpty()) {
                model.addAttribute("message", "Please select a file to upload.");
                return "redirect:/profile";
            }

            String username = principal.getName();

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.lastIndexOf('.') > 0) {
                extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }

            // Создать новое имя файла
            String objectName = "profile_image_" + username + extension;

            // Загрузка файла в MinIO
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

        } catch (IOException e) {
            System.out.println("Failed to upload file due to IO error: " + e.getMessage());
        } catch (MinioException e) {
            System.out.println("MinIO error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    public String getShareableLink(String objectName) {
        try {
            // Генерация временной ссылки на объект
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .method(Method.GET) // Указать метод GET
                    .expiry(60 * 60 * 24) // 1 день
                    .build();

            return minioClient.getPresignedObjectUrl(args);
        } catch (Exception e) {
            System.out.println("Error generating shareable link: " + e.getMessage());
            return null;
        }
    }
}
