package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.request.LegalDocumentRequest;
import com.landgo.coreservice.dto.response.ApiResponse;
import com.landgo.coreservice.dto.response.LegalDocumentResponse;
import com.landgo.coreservice.service.LegalDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/legal")
@RequiredArgsConstructor
@Tag(name = "Legal Content", description = "Endpoints for retrieving legal documents like Privacy Policy and Terms of Service")
public class LegalDocumentController {

    private final LegalDocumentService legalDocumentService;

    @GetMapping("/{documentType}")
    @Operation(summary = "Get legal document by type", description = "Retrieves HTML content for Privacy Policy or Terms of Service. Public endpoint.")
    public ResponseEntity<ApiResponse<LegalDocumentResponse>> getLegalDocument(@PathVariable String documentType) {
        LegalDocumentResponse response = legalDocumentService.getLegalDocument(documentType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{documentType}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create or update legal document by type", description = "Allows administrators to dynamically update Privacy Policy or Terms of Service HTML content. Requires admin role.")
    public ResponseEntity<ApiResponse<LegalDocumentResponse>> createOrUpdateLegalDocument(
            @PathVariable String documentType,
            @Valid @RequestBody LegalDocumentRequest request) {
        LegalDocumentResponse response = legalDocumentService.createOrUpdateLegalDocument(documentType, request);
        return ResponseEntity.ok(ApiResponse.success("Legal document updated successfully", response));
    }
}
