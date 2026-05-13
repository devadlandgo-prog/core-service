package com.landgo.coreservice.service.impl;

import com.landgo.coreservice.dto.request.ImageUploadRequest;
import com.landgo.coreservice.dto.response.PresignedUrlResponse;
import com.landgo.coreservice.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ImageStorageServiceImpl implements ImageStorageService {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${aws.s3.images-bucket}")
    private String bucketName;

    @Override
    public PresignedUrlResponse generatePresignedUrl(ImageUploadRequest request, String directory) {
        String fileExtension = "";
        if (request.getFileName() != null && request.getFileName().contains(".")) {
            fileExtension = request.getFileName().substring(request.getFileName().lastIndexOf("."));
        }
        
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        String fileKey = directory.endsWith("/") ? directory + uniqueFileName : directory + "/" + uniqueFileName;

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .contentType(request.getContentType())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        log.info("Generated presigned URL for fileKey: {}", fileKey);

        return PresignedUrlResponse.builder()
                .url(presignedRequest.url().toString())
                .fileKey(fileKey)
                .method("PUT")
                .build();
    }

    @Override
    public PresignedUrlResponse generatePresignedReadUrl(String fileKey, int expiryMinutes) {
        int clampedExpiry = Math.min(Math.max(expiryMinutes, 1), 720);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(clampedExpiry))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        log.info("Generated presigned read URL for fileKey: {} (expiry: {} min)", fileKey, clampedExpiry);

        return PresignedUrlResponse.builder()
                .url(presignedRequest.url().toString())
                .fileKey(fileKey)
                .method("GET")
                .build();
    }

    @Override
    public void deleteImage(String fileKey) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            log.info("Deleted image from S3: {}", fileKey);
        } catch (Exception e) {
            log.error("Failed to delete image from S3: {}", fileKey, e);
            throw new RuntimeException("Failed to delete image from S3", e);
        }
    }
}
