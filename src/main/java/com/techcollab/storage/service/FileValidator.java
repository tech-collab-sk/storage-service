package com.techcollab.storage.service;

import com.techcollab.storage.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class FileValidator {

    @Value("${storage.max-file-size}")
    private long maxFileSize;

    @Value("${storage.allowed-content-types}")
    private String allowedTypes;

    public void validate(MultipartFile file) {

        if (file.isEmpty()) {
            throw new StorageException("File cannot be empty");
        }

        if (file.getSize() > maxFileSize) { // bytes
            throw new StorageException("File size exceeds limit");
        }

        String contentType = file.getContentType();

        if (contentType == null) {
            throw new StorageException("Invalid file type");
        }

        List<String> patterns = Arrays.asList(allowedTypes.split(","));

        boolean matches = patterns.stream()
                .map(String::trim)
                .map(Pattern::compile)
                .anyMatch(pattern -> pattern.matcher(contentType).matches());

        if (!matches) {
            throw new StorageException("Invalid file type: " + contentType);
        }
    }
}