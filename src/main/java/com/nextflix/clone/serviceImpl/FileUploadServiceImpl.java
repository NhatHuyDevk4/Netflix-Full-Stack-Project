package com.nextflix.clone.serviceImpl;

import com.nextflix.clone.service.FileUploadService;
import com.nextflix.clone.util.FileHandleUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    private Path videoStorageLocation;
    private Path imageStorageLocation;

    @Value("${file.upload.video-dir:upload/videos}")
    private String videoDir;

    @Value("${file.upload.image-dir:upload/images}")
    private String imageDir;

    @PostConstruct
    public void init() {
        this.videoStorageLocation = Path.of(videoDir).toAbsolutePath().normalize();
        this.imageStorageLocation = Path.of(imageDir).toAbsolutePath().normalize();

        try {
            java.nio.file.Files.createDirectories(videoStorageLocation);
            java.nio.file.Files.createDirectories(imageStorageLocation);
        } catch (Exception e) {
            throw new RuntimeException("Could not create upload directories", e);
        }
    }

    @Override
    public String storeVideoFile(MultipartFile file) {
        return storeFile(file, videoStorageLocation);
    }

    private String storeFile(MultipartFile file, Path storageLocation) {
        String fileExtension = FileHandleUtils.extractFileExtension(file.getOriginalFilename());
        String uuid = UUID.randomUUID().toString();
        String fileName = uuid + fileExtension;

        try {
            if(file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file " + fileName);
            }

            Path targetLocation = storageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return uuid;
        } catch (IOException ex) {
            throw new RuntimeException("File upload failed: " + ex.getMessage(), ex);
        }
    }
}
