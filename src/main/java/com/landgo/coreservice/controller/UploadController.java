package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.request.ImageUploadRequest;
import com.landgo.coreservice.dto.request.PresignedReadUrlRequest;
import com.landgo.coreservice.dto.response.PresignedUrlResponse;
import com.landgo.coreservice.service.ImageStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
@Tag(name = "Global Uploads", description = "Endpoints for generic file uploads before entity creation")
public class UploadController {

    private final ImageStorageService imageStorageService;

    @PostMapping("/presigned-url")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Generate S3 presigned URL for files before entity creation", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PresignedUrlResponse> generateGenericPresignedUrl(
            @Valid @RequestBody ImageUploadRequest request) {
        
        String context = (request.getContext() != null && !request.getContext().trim().isEmpty()) 
                ? request.getContext().trim() 
                : "general";
                
        String directory;
        if ("reviews".equalsIgnoreCase(context) || "professionals".equalsIgnoreCase(context)) {
            // B-CON-04: Store reviews under professionals namespace
            directory = "uploads/professionals/" + context + "/" + UUID.randomUUID().toString();
        } else {
            // Store in a meaningful path: e.g. "uploads/listings/drafts/{uuid}"
            directory = "uploads/" + context + "/drafts/" + UUID.randomUUID().toString();
        }
        
        PresignedUrlResponse response = imageStorageService.generatePresignedUrl(request, directory);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/presigned-read-url")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Generate a pre-signed S3 GET URL from a stored fileKey",
        description = "Returns a temporary signed URL (default 60 min, max 720 min) that allows the client to fetch a private S3 object directly.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PresignedUrlResponse> generatePresignedReadUrl(
            @Valid @RequestBody PresignedReadUrlRequest request) {

        int expiry = (request.getExpiryMinutes() != null && request.getExpiryMinutes() > 0)
                ? request.getExpiryMinutes()
                : 60;

        PresignedUrlResponse response = imageStorageService.generatePresignedReadUrl(request.getFileKey(), expiry);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/presigned-url/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Generate a pre-signed S3 GET URL from a stored fileKey (alias)",
        description = "Backward-compatible alias for read-url generation using fileKey.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PresignedUrlResponse> generatePresignedReadUrlAlias(
            @Valid @RequestBody PresignedReadUrlRequest request) {
        return generatePresignedReadUrl(request);
    }
}
