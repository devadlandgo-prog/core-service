package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.request.LegalDocumentCreateRequest;
import com.landgo.coreservice.dto.request.LegalDocumentRequest;
import com.landgo.coreservice.dto.response.ApiResponse;
import com.landgo.coreservice.dto.response.LegalDocumentDeleteResponse;
import com.landgo.coreservice.dto.response.LegalDocumentResponse;
import com.landgo.coreservice.dto.response.LegalDocumentSummaryResponse;
import com.landgo.coreservice.service.LegalDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/legal")
@RequiredArgsConstructor
@Tag(name = "Legal Content", description = "Endpoints for retrieving and managing legal documents like Privacy Policy and Terms of Service")
public class LegalDocumentController {

    private final LegalDocumentService legalDocumentService;

    @GetMapping({"", "/"})
    @Operation(summary = "List all legal document types",
            description = "Returns every registered legal document as a summary — no contentHtml, "
                    + "since a listing is for building an admin table or a footer of links. "
                    + "Fetch GET /legal/{documentType} for the body. Public endpoint.")
    public ResponseEntity<ApiResponse<List<LegalDocumentSummaryResponse>>> listLegalDocuments() {
        List<LegalDocumentSummaryResponse> response = legalDocumentService.listLegalDocuments();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{documentType}")
    @Operation(summary = "Get legal document by type",
            description = "Retrieves HTML content for a legal document by its canonical slug or one of its aliases. Public endpoint.")
    public ResponseEntity<ApiResponse<LegalDocumentResponse>> getLegalDocument(@PathVariable String documentType) {
        LegalDocumentResponse response = legalDocumentService.getLegalDocument(documentType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping({"", "/"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a legal document type",
            description = "Registers a new legal document type with initial content. Slug must be lowercase kebab-case and must not collide with an existing type or alias. Requires admin role.")
    public ResponseEntity<ApiResponse<LegalDocumentResponse>> createLegalDocument(
            @Valid @RequestBody LegalDocumentCreateRequest request) {
        LegalDocumentResponse response = legalDocumentService.createLegalDocument(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Legal document type created", response));
    }

    @PutMapping("/{documentType}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update legal document by type",
            description = "Updates the content of an existing legal document. The slug is unchanged. Requires admin role.")
    public ResponseEntity<ApiResponse<LegalDocumentResponse>> createOrUpdateLegalDocument(
            @PathVariable String documentType,
            @Valid @RequestBody LegalDocumentRequest request) {
        LegalDocumentResponse response = legalDocumentService.createOrUpdateLegalDocument(documentType, request);
        return ResponseEntity.ok(ApiResponse.success("Legal document updated successfully", response));
    }

    @DeleteMapping("/{documentType}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a legal document type",
            description = "Permanently removes a custom legal document type. Protected types backing public pages (privacy, terms) cannot be deleted. Requires admin role.")
    public ResponseEntity<ApiResponse<LegalDocumentDeleteResponse>> deleteLegalDocument(@PathVariable String documentType) {
        LegalDocumentDeleteResponse response = legalDocumentService.deleteLegalDocument(documentType);
        return ResponseEntity.ok(ApiResponse.success("Legal document type deleted", response));
    }
}
