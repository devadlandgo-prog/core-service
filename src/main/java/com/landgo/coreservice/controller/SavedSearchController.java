package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.request.SavedSearchRequest;
import com.landgo.coreservice.dto.response.ApiResponse;
import com.landgo.coreservice.dto.response.PageResponse;
import com.landgo.coreservice.dto.response.SavedSearchResponse;
import com.landgo.coreservice.security.CurrentUser;
import com.landgo.coreservice.service.SavedSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/listings/saved-searches")
@RequiredArgsConstructor
public class SavedSearchController {

    private final SavedSearchService savedSearchService;

    @PostMapping
    public ResponseEntity<ApiResponse<SavedSearchResponse>> createSavedSearch(
            @CurrentUser UUID userId,
            @Valid @RequestBody SavedSearchRequest request) {
        SavedSearchResponse search = savedSearchService.createSavedSearch(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Saved search created successfully", search));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SavedSearchResponse>>> getMySavedSearches(
            @CurrentUser UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<SavedSearchResponse> searches = savedSearchService.getMySavedSearches(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(searches));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SavedSearchResponse>> getSavedSearch(
            @CurrentUser UUID userId,
            @PathVariable UUID id) {
        SavedSearchResponse search = savedSearchService.getSavedSearch(userId, id);
        return ResponseEntity.ok(ApiResponse.success(search));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SavedSearchResponse>> updateSavedSearch(
            @CurrentUser UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody SavedSearchRequest request) {
        SavedSearchResponse search = savedSearchService.updateSavedSearch(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success("Saved search updated successfully", search));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSavedSearch(
            @CurrentUser UUID userId,
            @PathVariable UUID id) {
        savedSearchService.deleteSavedSearch(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Saved search deleted successfully", null));
    }

    @PatchMapping("/{id}/notifications")
    public ResponseEntity<ApiResponse<SavedSearchResponse>> toggleNotifications(
            @CurrentUser UUID userId,
            @PathVariable UUID id) {
        SavedSearchResponse search = savedSearchService.toggleNotifications(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Notifications toggled", search));
    }
}
