package com.techcollab.storage.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String upload(MultipartFile file, String folder);

    void delete(String path);

    String generateSignedUrl(String path, int expirySeconds);
}