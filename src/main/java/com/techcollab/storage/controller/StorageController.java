package com.techcollab.storage.controller;

import com.techcollab.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam MultipartFile file, @RequestParam(defaultValue = "uploads") String folder) {
        String url = storageService.upload(file, folder);

        return ResponseEntity.ok(Map.of(
                "url", url,
                "folder", folder
        ));
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam String path) {
        storageService.delete(path);
        return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
    }

    @GetMapping("/signed-url")
    public ResponseEntity<?> signedUrl(@RequestParam String path, @RequestParam(defaultValue = "3600") int expiry) {
        String url = storageService.generateSignedUrl(path, expiry);
        return ResponseEntity.ok(Map.of("signedUrl", url));
    }
}