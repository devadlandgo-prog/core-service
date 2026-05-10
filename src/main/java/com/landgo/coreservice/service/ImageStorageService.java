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
     * Deletes an image from the storage.
     * @param fileKey the exact key of the file in the bucket
     */
    void deleteImage(String fileKey);
}
