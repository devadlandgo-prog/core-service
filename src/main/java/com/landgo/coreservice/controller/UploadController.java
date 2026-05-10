package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.request.ImageUploadRequest;
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
                
        // Store in a meaningful path: e.g. "uploads/listings/drafts/{uuid}"
        String directory = "uploads/" + context + "/drafts/" + UUID.randomUUID().toString();
        PresignedUrlResponse response = imageStorageService.generatePresignedUrl(request, directory);
        return ResponseEntity.ok(response);
    }
}
