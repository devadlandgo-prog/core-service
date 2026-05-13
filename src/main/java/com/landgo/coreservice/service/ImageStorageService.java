package com.landgo.coreservice.service;

import com.landgo.coreservice.dto.request.ImageUploadRequest;
import com.landgo.coreservice.dto.response.PresignedUrlResponse;

public interface ImageStorageService {
    
    /**
     * Generates a pre-signed URL for the client to upload an image directly to S3.
     * @param request the upload request containing file details
     * @param directory the directory prefix (e.g., "listings/123")
     * @return PresignedUrlResponse containing the URL and the generated file key
     */
    PresignedUrlResponse generatePresignedUrl(ImageUploadRequest request, String directory);

    /**
     * Generates a pre-signed GET URL so the client can read/display a private S3 object.
     * @param fileKey  the exact S3 object key (e.g. "uploads/listings/drafts/uuid/file.jpg")
     * @param expiryMinutes how long the URL should be valid (1–720 minutes)
     * @return PresignedUrlResponse containing the signed GET URL, the fileKey, and method "GET"
     */
    PresignedUrlResponse generatePresignedReadUrl(String fileKey, int expiryMinutes);

    /**
     * Deletes an image from the storage.
     * @param fileKey the exact key of the file in the bucket
     */
    void deleteImage(String fileKey);
}
