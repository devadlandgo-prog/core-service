package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.request.FaqReorderRequest;
import com.landgo.coreservice.dto.request.FaqRequest;
import com.landgo.coreservice.dto.response.ApiResponse;
import com.landgo.coreservice.dto.response.FaqResponse;
import com.landgo.coreservice.service.FaqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Admin FAQ administration — the source of truth for what {@code /faq} shows.
 *
 * <p>There are intentionally no publish/unpublish or draft endpoints: a saved FAQ is live.
 */
@RestController
@RequestMapping("/admin/faqs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "FAQ Administration", description = "Admin-only FAQ management (requires an admin JWT)")
public class AdminFaqController {

    private final FaqService faqService;

    @GetMapping({"", "/"})
    @Operation(summary = "List all non-deleted FAQs", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<FaqResponse>>> listFaqs(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(faqService.listAdminFaqs(category, search)));
    }

    @PostMapping({"", "/"})
    @Operation(summary = "Create and publish an FAQ", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<FaqResponse>> createFaq(@Valid @RequestBody FaqRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("FAQ published", faqService.createFaq(request)));
    }

    /**
     * Reorder is declared before {@code /{id}} so "reorder" is never parsed as a UUID path variable.
     */
    @PatchMapping("/reorder")
    @Operation(summary = "Persist reordered sortOrder values", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<FaqResponse>>> reorder(@Valid @RequestBody FaqReorderRequest request) {
        return ResponseEntity.ok(ApiResponse.success("FAQ order updated", faqService.reorder(request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Read one FAQ for editing", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<FaqResponse>> getFaq(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(faqService.getFaq(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace a published FAQ", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<FaqResponse>> replaceFaq(
            @PathVariable UUID id, @Valid @RequestBody FaqRequest request) {
        return ResponseEntity.ok(ApiResponse.success("FAQ updated", faqService.updateFaq(id, request, false)));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Edit selected fields of a published FAQ", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<FaqResponse>> patchFaq(
            @PathVariable UUID id, @RequestBody FaqRequest request) {
        return ResponseEntity.ok(ApiResponse.success("FAQ updated", faqService.updateFaq(id, request, true)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete an FAQ", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteFaq(@PathVariable UUID id) {
        faqService.deleteFaq(id);
        return ResponseEntity.ok(ApiResponse.success("FAQ deleted", null));
    }
}
