package pro.akosarev.sandbox.service;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import pro.akosarev.sandbox.repository.PathRepository;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {

    PathRepository pathRepository;

    public ImageService(PathRepository pathRepository) {
        this.pathRepository = pathRepository;
    }

    public String  saveImageFolder(MultipartFile imageFile) throws IOException {

        String pathMain = pathRepository.findByName("fullImage").getPath();

        byte[] bytes = imageFile.getBytes();

        String randomFileName = UUID.randomUUID().toString().substring(0, 16); // Имя случайное
        String originalFilename = imageFile.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));

        String fullPath = pathMain + randomFileName + fileExtension;
        Path path = Paths.get("." + fullPath);

        Files.write(path, bytes);

        return randomFileName + fileExtension;
    }

}
