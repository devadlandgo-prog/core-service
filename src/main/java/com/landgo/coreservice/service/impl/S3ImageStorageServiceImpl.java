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

    /**
     * Turns a stored media reference into a URL a client can actually load.
     *
     * <p>The images bucket is private. Listing photos were persisted as plain
     * {@code https://bucket.s3.region.amazonaws.com/key} URLs, which answer 403 with an XML body —
     * browsers and image optimisers report that as a broken image, not a permissions problem,
     * which is why it read as corrupt uploads. Anything pointing at our own bucket is re-signed
     * here, whether it was stored as a key or a full URL; genuinely external URLs pass through.
     *
     * @return a loadable URL, or the original value when it is not ours to sign
     */
    @Override
    public String toViewableUrl(String stored, int expiryMinutes) {
        if (stored == null || stored.isBlank()) {
            return null;
        }
        String value = stored.trim();
        String key = extractOwnBucketKey(value);
        if (key == null) {
            return value;
        }
        try {
            return generatePresignedReadUrl(key, expiryMinutes).getUrl();
        } catch (Exception e) {
            log.error("Failed to sign media reference {}: {}", value, e.getMessage());
            return value;
        }
    }

    /**
     * The object key when {@code value} refers to our images bucket, otherwise null.
     *
     * <p>Handles both S3 URL styles — {@code bucket.s3.region.amazonaws.com/key} and
     * {@code s3.region.amazonaws.com/bucket/key} — plus a bare key.
     */
    private String extractOwnBucketKey(String value) {
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            return value;
        }
        if (bucketName == null || bucketName.isBlank() || !value.contains(bucketName)) {
            return null;
        }
        try {
            java.net.URI uri = java.net.URI.create(value);
            String host = uri.getHost() == null ? "" : uri.getHost();
            String path = uri.getPath() == null ? "" : uri.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            if (host.startsWith(bucketName + ".")) {
                return path.isBlank() ? null : path;
            }
            String prefix = bucketName + "/";
            if (path.startsWith(prefix)) {
                String key = path.substring(prefix.length());
                return key.isBlank() ? null : key;
            }
            return null;
        } catch (IllegalArgumentException e) {
            log.warn("Could not parse stored media URL: {}", value);
            return null;
        }
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
