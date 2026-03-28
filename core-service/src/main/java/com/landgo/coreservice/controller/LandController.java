package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.request.LandCreateRequest;
import com.landgo.coreservice.dto.response.ApiResponse;
import com.landgo.coreservice.dto.response.LandResponse;
import com.landgo.coreservice.dto.response.PageResponse;
import com.landgo.coreservice.enums.LandStatus;
import com.landgo.coreservice.enums.ProjectStage;
import com.landgo.coreservice.security.CurrentUser;
import com.landgo.coreservice.service.LandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lands")
@RequiredArgsConstructor
public class LandController {

    private final LandService landService;

    @PostMapping
    public ResponseEntity<ApiResponse<LandResponse>> createLand(
            @CurrentUser UUID userId,
            @Valid @RequestBody LandCreateRequest request) {
        LandResponse land = landService.createLand(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Land listing created successfully", land));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LandResponse>> getLandById(@PathVariable UUID id) {
        LandResponse land = landService.getLandByIdWithView(id);
        return ResponseEntity.ok(ApiResponse.success(land));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LandResponse>>> getActiveListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<LandResponse> lands = landService.getActiveListings(page, size);
        return ResponseEntity.ok(ApiResponse.success(lands));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<LandResponse>>> searchLands(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<LandResponse> lands = landService.searchLands(q, page, size);
        return ResponseEntity.ok(ApiResponse.success(lands));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<PageResponse<LandResponse>>> filterLands(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) ProjectStage stage,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<LandResponse> lands = landService.filterLands(city, stage, minPrice, maxPrice, page, size);
        return ResponseEntity.ok(ApiResponse.success(lands));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<ApiResponse<PageResponse<LandResponse>>> getVendorLands(
            @PathVariable UUID vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<LandResponse> lands = landService.getVendorLands(vendorId, page, size);
        return ResponseEntity.ok(ApiResponse.success(lands));
    }

    @GetMapping("/my-listings")
    public ResponseEntity<ApiResponse<PageResponse<LandResponse>>> getMyListings(
            @CurrentUser UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<LandResponse> lands = landService.getMyListings(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(lands));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LandResponse>> updateLand(
            @CurrentUser UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody LandCreateRequest request) {
        LandResponse land = landService.updateLand(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success("Land listing updated successfully", land));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLand(
            @CurrentUser UUID userId,
            @PathVariable UUID id) {
        landService.deleteLand(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Land listing deleted successfully", null));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<LandResponse>> updateLandStatus(
            @PathVariable UUID id,
            @RequestParam LandStatus status) {
        LandResponse land = landService.updateLandStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Land status updated", land));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<LandResponse>>> getRecentListings(
            @RequestParam(defaultValue = "10") int limit) {
        List<LandResponse> lands = landService.getRecentListings(limit);
        return ResponseEntity.ok(ApiResponse.success(lands));
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<LandResponse>>> getPopularListings(
            @RequestParam(defaultValue = "10") int limit) {
        List<LandResponse> lands = landService.getPopularListings(limit);
        return ResponseEntity.ok(ApiResponse.success(lands));
    }
}
