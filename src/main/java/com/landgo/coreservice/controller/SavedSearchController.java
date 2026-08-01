package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.request.SavedSearchRequest;
import com.landgo.coreservice.dto.response.ApiResponse;
import com.landgo.coreservice.dto.response.PageResponse;
import com.landgo.coreservice.dto.response.SavedSearchResponse;
import com.landgo.coreservice.security.CurrentUser;
import com.landgo.coreservice.service.SavedSearchService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/listings/saved-searches")
@RequiredArgsConstructor
@Tag(name = "Saved Searches", description = "Save and manage listing search filters; toggle push notification alerts per search")
public class SavedSearchController {

    private final SavedSearchService savedSearchService;

    @PostMapping
    @Operation(summary = "Create a saved search")
    public ResponseEntity<ApiResponse<SavedSearchResponse>> createSavedSearch(
            @CurrentUser UUID userId,
            @Valid @RequestBody SavedSearchRequest request) {
        SavedSearchResponse search = savedSearchService.createSavedSearch(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Saved search created successfully", search));
    }

    @GetMapping
    @Operation(summary = "List my saved searches")
    public ResponseEntity<ApiResponse<PageResponse<SavedSearchResponse>>> getMySavedSearches(
            @CurrentUser UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<SavedSearchResponse> searches = savedSearchService.getMySavedSearches(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(searches));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a saved search by ID")
    public ResponseEntity<ApiResponse<SavedSearchResponse>> getSavedSearch(
            @CurrentUser UUID userId,
            @PathVariable UUID id) {
        SavedSearchResponse search = savedSearchService.getSavedSearch(userId, id);
        return ResponseEntity.ok(ApiResponse.success(search));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a saved search")
    public ResponseEntity<ApiResponse<SavedSearchResponse>> updateSavedSearch(
            @CurrentUser UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody SavedSearchRequest request) {
        SavedSearchResponse search = savedSearchService.updateSavedSearch(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success("Saved search updated successfully", search));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a saved search")
    public ResponseEntity<ApiResponse<Void>> deleteSavedSearch(
            @CurrentUser UUID userId,
            @PathVariable UUID id) {
        savedSearchService.deleteSavedSearch(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Saved search deleted successfully", null));
    }

    @PatchMapping("/{id}/notifications")
    @Operation(summary = "Toggle push notifications for a saved search", description = "Enables or disables listing-alert push notifications for this saved search criteria.")
    public ResponseEntity<ApiResponse<SavedSearchResponse>> toggleNotifications(
            @CurrentUser UUID userId,
            @PathVariable UUID id) {
        SavedSearchResponse search = savedSearchService.toggleNotifications(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Notifications toggled", search));
    }
}
