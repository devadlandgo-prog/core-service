package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.request.DraftStepRequest;
import com.landgo.coreservice.dto.response.ApiResponse;
import com.landgo.coreservice.dto.response.DraftResponse;
import com.landgo.coreservice.dto.response.PageResponse;
import com.landgo.coreservice.security.CurrentUser;
import com.landgo.coreservice.service.ListingDraftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@RestController
@RequestMapping("/listings/drafts")
@RequiredArgsConstructor
@Tag(name = "Listing Drafts", description = "Step-by-step draft management before a listing is published")
public class DraftController {

    private final ListingDraftService draftService;

    @PostMapping
    @Operation(summary = "Create a new draft", description = "Creates an empty draft for the authenticated vendor.")
    public ResponseEntity<ApiResponse<DraftResponse>> createDraft(@CurrentUser UUID userId) {
        DraftResponse draft = draftService.createDraft(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Draft created successfully", draft));
    }

    @PatchMapping("/{draftId}")
    @Operation(summary = "Save a draft step", description = "Partially updates a draft with the fields provided in this step.")
    public ResponseEntity<ApiResponse<DraftResponse>> updateDraftStep(
            @CurrentUser UUID userId,
            @PathVariable UUID draftId,
            @Valid @RequestBody DraftStepRequest request) {
        DraftResponse draft = draftService.updateDraftStep(userId, draftId, request);
        return ResponseEntity.ok(ApiResponse.success("Draft step updated", draft));
    }

    @GetMapping("/{draftId}")
    @Operation(summary = "Get a draft by ID")
    public ResponseEntity<ApiResponse<DraftResponse>> getDraft(
            @CurrentUser UUID userId,
            @PathVariable UUID draftId) {
        DraftResponse draft = draftService.getDraft(userId, draftId);
        return ResponseEntity.ok(ApiResponse.success(draft));
    }

    @GetMapping
    @Operation(summary = "List my drafts", description = "Returns all drafts owned by the authenticated vendor.")
    public ResponseEntity<ApiResponse<PageResponse<DraftResponse>>> getMyDrafts(
            @CurrentUser UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<DraftResponse> drafts = draftService.getMyDrafts(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(drafts));
    }

    @DeleteMapping("/{draftId}")
    @Operation(summary = "Delete a draft")
    public ResponseEntity<ApiResponse<Void>> deleteDraft(
            @CurrentUser UUID userId,
            @PathVariable UUID draftId) {
        draftService.deleteDraft(userId, draftId);
        return ResponseEntity.ok(ApiResponse.success("Draft deleted successfully", null));
    }

    @PostMapping("/{draftId}/publish")
    @Operation(summary = "Mark draft as published", description = "Transitions the draft to published state after the listing has been saved.")
    public ResponseEntity<ApiResponse<DraftResponse>> publishDraft(
            @CurrentUser UUID userId,
            @PathVariable UUID draftId) {
        DraftResponse draft = draftService.markAsPublished(userId, draftId);
        return ResponseEntity.ok(ApiResponse.success("Draft published successfully", draft));
    }
}
