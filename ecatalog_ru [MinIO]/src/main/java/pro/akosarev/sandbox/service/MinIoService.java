package pro.akosarev.sandbox.service;

import io.minio.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.akosarev.sandbox.entity.File;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class MinIoService {
    MinioClient minioClient;

    public MinIoService(MinioClient minioClient){
        this.minioClient = minioClient;
    }
    private String bucketName = "ecatalog-bucket";

    public List<File> getListObjects() {
        List<File> objects = new ArrayList<>();
        try {
            Iterable<Result<Item>> result = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .recursive(true)
                    .build());
            for (Result<Item> item : result) {
                objects.add(File.builder()
                        .filename(item.get().objectName())
                        .size(item.get().size())
                        .url(getPreSignedUrl(item.get().objectName()))
                        .build());
            }
            return objects;
        } catch (Exception e) {
            System.out.println("Happened error when get list objects from minio");
        }

        return objects;
    }

    private String getPreSignedUrl(String filename) {
        return "http://localhost:8080/file/".concat(filename);
    }

    public File uploadFile(File request) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(request.getFile().getOriginalFilename())
                    .stream(request.getFile().getInputStream(), request.getFile().getSize(), -1)
                    .build());
        } catch (Exception e) {
            System.out.println("Happened error when upload file");
        }
        return File.builder()
                .size(request.getFile().getSize())
                .url(getPreSignedUrl(request.getFile().getOriginalFilename()))
                .filename(request.getFile().getOriginalFilename())
                .build();
    }

    public File uploadImageToMinioFromUrl(String imageUrl, String imageName) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            byte[] imageData = restTemplate.getForObject(imageUrl, byte[].class);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(imageName)
                    .stream(inputStream, inputStream.available(), -1)
                    .build());

            return File.builder()
                    .size((long) imageData.length)
                    .url(getPreSignedUrl(imageName))
                    .filename(imageName)
                    .build();

        } catch (Exception e) {
            System.out.println(imageName);
            e.printStackTrace();
        }

        return null;
    }

    public InputStream getObject(String filename) {
        InputStream stream;
        try {
            stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .build());

        } catch (Exception e) {
            System.out.println("Happened error when get list objects from minio");
            return null;
        }

        return stream;
    }

    public void deleteObject(String filename) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .build());
        } catch (Exception e) {
            System.out.println("Error occurred when deleting object from MinIo");
        }
    }
}
