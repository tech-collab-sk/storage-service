package com.techcollab.storage.client;

import com.techcollab.storage.exception.StorageException;
import com.techcollab.storage.service.FileValidator;
import com.techcollab.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupabaseStorageService implements StorageService {

    private final FileValidator fileValidator;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${storage.bucket}")
    private String bucket;

    @Value("${storage.public}")
    private boolean isPublic;

    @Value("${storage.endpoint}")
    private String endpoint;

    @Override
    public String upload(MultipartFile file, String folder) {

        log.info("*** [SupabaseStorageService] :: [upload] :: Uploading file: {} to folder: {}", file.getOriginalFilename(), folder);

        fileValidator.validate(file);

        String fileName = file.getOriginalFilename();
        String key = folder + "/" + fileName;

        try {

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(file.getBytes())
            );

            if (isPublic) {
                return endpoint.replace("/s3", "")
                        + "/object/public/"
                        + bucket + "/" + key;
            }

            return generateSignedUrl(key, 3600);

        } catch (Exception e) {
            throw new StorageException("Upload failed", e);
        }
    }

    @Override
    public void delete(String path) {

        log.info("*** [SupabaseStorageService] :: [delete] :: Deleting file at path: {}", path);
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(path)
                    .build();

            s3Client.deleteObject(request);

        } catch (Exception e) {
            throw new StorageException("Delete failed", e);
        }
    }


    @Override
    public String generateSignedUrl(String path, int expirySeconds) {

        log.info("*** [SupabaseStorageService] :: [generateSignedUrl] :: Generating signed URL for path: {} with expiry: {} seconds", path, expirySeconds);
        try {

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(path)
                    .build();

            GetObjectPresignRequest presignRequest =
                    GetObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofSeconds(expirySeconds))
                            .getObjectRequest(getObjectRequest)
                            .build();

            return s3Presigner.presignGetObject(presignRequest)
                    .url()
                    .toString();

        } catch (Exception e) {
            throw new StorageException("Signed URL generation failed", e);
        }
    }
}