package pro.akosarev.sandbox.controller;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@Controller
public class FileUploadController {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket.name}")
    private String bucketName;

    @PostMapping("/upload")
    public String uploadFile(Model model, MultipartFile file, Principal principal) {
        try {
            if (file.isEmpty()) {
                model.addAttribute("message", "Please select a file to upload.");
                return "profile"; // Вернуться к странице загрузки с сообщением
            }

            // Получить имя пользователя из объекта Principal
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

        } catch (IOException e) {
            System.out.println("Failed to upload file due to IO error: " + e.getMessage());
        } catch (MinioException e) {
            System.out.println("MinIO error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }

        return "profile";
    }
}
