package com.nextflix.clone.serviceImpl;

import com.nextflix.clone.service.FileUploadService;
import com.nextflix.clone.util.FileHandleUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @Override
    public String storeImageFile(MultipartFile file) {
        return storeFile(file, imageStorageLocation);
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


    @Override
    public ResponseEntity<Resource> serviceVideo(String uuid, String rangeHeader) {
        try {
            Path filePath = FileHandleUtils.findFileByUuid(videoStorageLocation, uuid);
            Resource resource = FileHandleUtils.createFullResource(filePath);

            String fileName = resource.getFilename();
            String contentType = FileHandleUtils.detectVideoContentType(fileName);

            long fileLength = resource.contentLength();

            if(isFullContentRequest(rangeHeader)) {
                return buildFullVideoResponse(resource, contentType, fileLength);
            }

            return buildPartVideoResponse(fileName, contentType, fileLength, rangeHeader, filePath);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<Resource> serveImage(String uuid) {
        try {
            Path filePath = FileHandleUtils.findFileByUuid(imageStorageLocation, uuid);
            Resource resource = FileHandleUtils.createFullResource(filePath);

            String fileName = resource.getFilename();
            String contentType = FileHandleUtils.detectImageContentType(fileName);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    private ResponseEntity<Resource> buildFullVideoResponse(Resource resource, String contentType, long fileLength) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileLength))
                .body(resource);
    }

    private ResponseEntity<Resource> buildPartVideoResponse(String fileName, String contentType, long fileLength, String rangeHeader, Path filePath) throws IOException {
        Long[] range = FileHandleUtils.parseRangeHeader(rangeHeader, fileLength);
        long rangeStart = range[0];
        long rangeEnd = range[1];

        if(!isValidRange(rangeStart, rangeEnd, fileLength)) {
            return buildRangeNotSatisfiableResponse(fileLength);
        }

        long contentLength = rangeEnd - rangeStart + 1;
        Resource resource = FileHandleUtils.createResourceFromFile(filePath, rangeStart, contentLength);

        return ResponseEntity.status(206)
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                .body(resource);
    }

    private ResponseEntity<Resource> buildRangeNotSatisfiableResponse(long fileLength) {
        return ResponseEntity.status(416)
                .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileLength)
                .build();
    }

    private boolean isValidRange(long rangeStart, long rangeEnd, long fileLength) {
        return rangeStart >= 0 && rangeEnd < fileLength && rangeStart <= rangeEnd;
    }

    private boolean isFullContentRequest(String rangeHeader) {
        return rangeHeader == null || rangeHeader.isBlank() || rangeHeader.isEmpty();

    }
}
