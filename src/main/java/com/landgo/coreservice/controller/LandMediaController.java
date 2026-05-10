package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.request.ImageConfirmRequest;
import com.landgo.coreservice.dto.request.ImageUploadRequest;
import com.landgo.coreservice.dto.response.LandResponse;
import com.landgo.coreservice.dto.response.PresignedUrlResponse;
import com.landgo.coreservice.service.ImageStorageService;
import com.landgo.coreservice.service.LandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/listings/{id}")
@RequiredArgsConstructor
@Tag(name = "Land Media", description = "Endpoints for managing listing images and documents")
public class LandMediaController {

    private final ImageStorageService imageStorageService;
    private final LandService landService;

    // ==========================================
    // IMAGES
    // ==========================================

    @PostMapping("/images/presigned-url")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Generate S3 presigned URL for direct image upload", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PresignedUrlResponse> generateImagePresignedUrl(
            @PathVariable UUID id,
            @Valid @RequestBody ImageUploadRequest request,
            Principal principal) {
        
        String directory = "listings/" + id.toString() + "/images";
        PresignedUrlResponse response = imageStorageService.generatePresignedUrl(request, directory);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/images/confirm")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Confirm image upload and save metadata", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<LandResponse> confirmImageUpload(
            @PathVariable UUID id,
            @Valid @RequestBody ImageConfirmRequest request,
            Principal principal) {
        
        UUID vendorId = UUID.fromString(principal.getName());
        LandResponse response = landService.addImageMetadata(id, vendorId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/images/{fileKey}")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Delete an image from S3 and metadata", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<LandResponse> deleteImage(
            @PathVariable UUID id,
            @PathVariable String fileKey,
            Principal principal) {
        
        UUID vendorId = UUID.fromString(principal.getName());
        LandResponse response = landService.removeImageMetadata(id, vendorId, fileKey);
        imageStorageService.deleteImage(fileKey);
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // DOCUMENTS
    // ==========================================

    @PostMapping("/documents/presigned-url")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Generate S3 presigned URL for direct document upload", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PresignedUrlResponse> generateDocumentPresignedUrl(
            @PathVariable UUID id,
            @Valid @RequestBody ImageUploadRequest request,
            Principal principal) {
        
        String directory = "listings/" + id.toString() + "/documents";
        PresignedUrlResponse response = imageStorageService.generatePresignedUrl(request, directory);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/documents/confirm")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Confirm document upload and save metadata", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<LandResponse> confirmDocumentUpload(
            @PathVariable UUID id,
            @Valid @RequestBody ImageConfirmRequest request,
            Principal principal) {
        
        UUID vendorId = UUID.fromString(principal.getName());
        LandResponse response = landService.addDocumentMetadata(id, vendorId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/documents/{fileKey}")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Delete a document from S3 and metadata", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<LandResponse> deleteDocument(
            @PathVariable UUID id,
            @PathVariable String fileKey,
            Principal principal) {
        
        UUID vendorId = UUID.fromString(principal.getName());
        LandResponse response = landService.removeDocumentMetadata(id, vendorId, fileKey);
        imageStorageService.deleteImage(fileKey);
        return ResponseEntity.ok(response);
    }
}
