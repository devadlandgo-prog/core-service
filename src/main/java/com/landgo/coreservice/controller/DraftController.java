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

import java.util.UUID;

@RestController
@RequestMapping("/listings/drafts")
@RequiredArgsConstructor
public class DraftController {

    private final ListingDraftService draftService;

    @PostMapping
    public ResponseEntity<ApiResponse<DraftResponse>> createDraft(@CurrentUser UUID userId) {
        DraftResponse draft = draftService.createDraft(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Draft created successfully", draft));
    }

    @PatchMapping("/{draftId}")
    public ResponseEntity<ApiResponse<DraftResponse>> updateDraftStep(
            @CurrentUser UUID userId,
            @PathVariable UUID draftId,
            @Valid @RequestBody DraftStepRequest request) {
        DraftResponse draft = draftService.updateDraftStep(userId, draftId, request);
        return ResponseEntity.ok(ApiResponse.success("Draft step updated", draft));
    }

    @GetMapping("/{draftId}")
    public ResponseEntity<ApiResponse<DraftResponse>> getDraft(
            @CurrentUser UUID userId,
            @PathVariable UUID draftId) {
        DraftResponse draft = draftService.getDraft(userId, draftId);
        return ResponseEntity.ok(ApiResponse.success(draft));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DraftResponse>>> getMyDrafts(
            @CurrentUser UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<DraftResponse> drafts = draftService.getMyDrafts(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(drafts));
    }

    @DeleteMapping("/{draftId}")
    public ResponseEntity<ApiResponse<Void>> deleteDraft(
            @CurrentUser UUID userId,
            @PathVariable UUID draftId) {
        draftService.deleteDraft(userId, draftId);
        return ResponseEntity.ok(ApiResponse.success("Draft deleted successfully", null));
    }

    @PostMapping("/{draftId}/publish")
    public ResponseEntity<ApiResponse<DraftResponse>> publishDraft(
            @CurrentUser UUID userId,
            @PathVariable UUID draftId) {
        DraftResponse draft = draftService.markAsPublished(userId, draftId);
        return ResponseEntity.ok(ApiResponse.success("Draft published successfully", draft));
    }
}
