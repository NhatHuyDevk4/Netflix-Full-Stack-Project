package com.nextflix.clone.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    String storeVideoFile(MultipartFile file);

    String storeImageFile(MultipartFile file);

    ResponseEntity<Resource> serviceVideo(String uuid, String rangeHeader);

    ResponseEntity<Resource> serveImage(String uuid);
}
