package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.request.LandCreateRequest;
import com.landgo.coreservice.dto.response.ApiResponse;
import com.landgo.coreservice.dto.response.LandResponse;
import com.landgo.coreservice.dto.response.PageResponse;
import com.landgo.coreservice.enums.LandStatus;
import com.landgo.coreservice.enums.ProjectStage;
import com.landgo.coreservice.enums.ProjectType;
import com.landgo.coreservice.enums.BuildingType;
import com.landgo.coreservice.enums.ZoningType;
import com.landgo.coreservice.enums.ListingType;
import com.landgo.coreservice.security.CurrentUser;
import com.landgo.coreservice.service.LandService;
import com.landgo.coreservice.dto.request.EnquiryRequest;
import com.landgo.coreservice.service.EnquiryService;
import com.landgo.coreservice.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/listings")
@RequiredArgsConstructor
@Tag(name = "Land Listings", description = "Endpoints for managing and searching land listings")
public class LandController {

    private final LandService landService;
    private final EnquiryService enquiryService;

    @PostMapping
    @Operation(summary = "Create a new land listing", description = "Allows vendors to create a new land listing. Status will be PENDING_APPROVAL by default.")
    public ResponseEntity<ApiResponse<LandResponse>> createLand(
            @CurrentUser UUID userId,
            @Valid @RequestBody LandCreateRequest request) {
        LandResponse land = landService.createLand(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Land listing created successfully", land));
    }

    @GetMapping("/favorites")
    @Operation(summary = "Get user's favorite land listings")
    public ResponseEntity<ApiResponse<List<LandResponse>>> getFavoriteLands(
            @CurrentUser UUID userId) {
        List<LandResponse> lands = landService.getFavoriteLands(userId);
        return ResponseEntity.ok(ApiResponse.success(lands));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get land listing details by ID")
    public ResponseEntity<ApiResponse<LandResponse>> getLandById(@PathVariable UUID id) {
        LandResponse land = landService.getLandById(id, null);
        return ResponseEntity.ok(ApiResponse.success(land));
    }

    @GetMapping
    @Operation(summary = "Get all active land listings (paginated)")
    public ResponseEntity<ApiResponse<PageResponse<LandResponse>>> getAllLands(
            @CurrentUser UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PageResponse<LandResponse> lands = landService.getActiveLands(page, size, sortBy, sortDir, userId);
        return ResponseEntity.ok(ApiResponse.success(lands));
    }

    @GetMapping("/search")
    @Operation(summary = "Search land listings by keyword (address or city)")
    public ResponseEntity<ApiResponse<PageResponse<LandResponse>>> searchLands(
            @CurrentUser UUID userId,
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<LandResponse> lands = landService.searchLands(q, page, size, userId);
        return ResponseEntity.ok(ApiResponse.success(lands));
    }

    @GetMapping("/filter-options/project-stage")
    @Operation(summary = "Get available project stage options")
    public ResponseEntity<ApiResponse<List<String>>> getProjectStageOptions() {
        List<String> options = Arrays.stream(ProjectStage.values())
                .map(ProjectStage::name)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(options));
    }

    @GetMapping("/filter-options/project-type")
    @Operation(summary = "Get available project type options")
    public ResponseEntity<ApiResponse<List<String>>> getProjectTypeOptions() {
        List<String> options = Arrays.stream(ProjectType.values())
                .map(ProjectType::name)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(options));
    }

    @GetMapping("/filter-options/building-type")
    @Operation(summary = "Get available building type options")
    public ResponseEntity<ApiResponse<List<String>>> getBuildingTypeOptions() {
        List<String> options = Arrays.stream(BuildingType.values())
                .map(BuildingType::name)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(options));
    }

    @GetMapping("/filter-options/zoning-type")
    @Operation(summary = "Get available zoning type options")
    public ResponseEntity<ApiResponse<List<String>>> getZoningTypeOptions() {
        List<String> options = Arrays.stream(ZoningType.values())
                .map(ZoningType::name)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(options));
    }

    @GetMapping("/filter-options/listing-type")
    @Operation(summary = "Get available listing type options")
    public ResponseEntity<ApiResponse<List<String>>> getListingTypeOptions() {
        List<String> options = Arrays.stream(ListingType.values())
                .map(ListingType::name)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(options));
    }

    @GetMapping("/filter")
    @Operation(summary = "Advanced filter for land listings",
               description = "Highly flexible filtering by city/q, stage, price range, lot size, project/building/zoning/listing types, and for-sale duration. Supports sorting by vendor rating, reviews, and experience.")
    public ResponseEntity<ApiResponse<PageResponse<LandResponse>>> filterLands(
            @CurrentUser UUID userId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) BigDecimal minLotSize,
            @RequestParam(required = false) BigDecimal maxLotSize,
            @RequestParam(required = false) Boolean isFeatured,
            @RequestParam(required = false) Boolean isHotDeal,
            @RequestParam(required = false) String projectType,
            @RequestParam(required = false) String buildingType,
            @RequestParam(required = false) String zoningType,
            @RequestParam(required = false) String listingType,
            @RequestParam(required = false) Integer forSaleSince,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String location = (city != null && !city.isBlank()) ? city : q;
        ProjectStage parsedStage = parseEnumToken(ProjectStage.class, stage, "stage");
        ProjectType parsedProjectType = parseEnumToken(ProjectType.class, projectType, "projectType");
        BuildingType parsedBuildingType = parseEnumToken(BuildingType.class, buildingType, "buildingType");
        ZoningType parsedZoningType = parseEnumToken(ZoningType.class, zoningType, "zoningType");
        ListingType parsedListingType = parseEnumToken(ListingType.class, listingType, "listingType");

        PageResponse<LandResponse> lands = landService.filterLands(
                location, parsedStage, minPrice, maxPrice, minLotSize, maxLotSize, isFeatured, isHotDeal,
                parsedProjectType != null ? parsedProjectType.name() : null,
                parsedBuildingType != null ? parsedBuildingType.name() : null,
                parsedZoningType != null ? parsedZoningType.name() : null,
                parsedListingType != null ? parsedListingType.name() : null,
                forSaleSince, sortBy,
                page, size, userId);
        return ResponseEntity.ok(ApiResponse.success(lands));
    }

    private <E extends Enum<E>> E parseEnumToken(Class<E> enumType, String rawValue, String fieldName) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        String normalized = rawValue.trim().replace(' ', '_').replace('-', '_').toUpperCase();
        try {
            return Enum.valueOf(enumType, normalized);
        } catch (IllegalArgumentException ex) {
            throw new com.landgo.coreservice.exception.BadRequestException(
                    "Invalid " + fieldName + " value: " + rawValue,
                    "VALIDATION_ERROR");
        }
    }

    @GetMapping("/mine")
    @Operation(summary = "Get listings owned by the current vendor")
    public ResponseEntity<ApiResponse<PageResponse<LandResponse>>> getMyLands(
            @CurrentUser UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<LandResponse> lands = landService.getMyLands(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(lands));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recently added listings")
    public ResponseEntity<ApiResponse<List<LandResponse>>> getRecentLands(
            @CurrentUser UUID userId,
            @RequestParam(defaultValue = "10") int limit) {
        List<LandResponse> lands = landService.getRecentLands(limit, userId);
        return ResponseEntity.ok(ApiResponse.success(lands));
    }

    @GetMapping("/popular")
    @Operation(summary = "Get most viewed listings")
    public ResponseEntity<ApiResponse<List<LandResponse>>> getPopularLands(
            @CurrentUser UUID userId,
            @RequestParam(defaultValue = "10") int limit) {
        List<LandResponse> lands = landService.getPopularLands(limit, userId);
        return ResponseEntity.ok(ApiResponse.success(lands));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured listings")
    public ResponseEntity<ApiResponse<List<LandResponse>>> getFeaturedLands(
            @CurrentUser UUID userId,
            @RequestParam(defaultValue = "10") int limit) {
        List<LandResponse> lands = landService.getFeaturedLands(limit, userId);
        return ResponseEntity.ok(ApiResponse.success(lands));
    }

    @GetMapping("/hot-developer")
    @Operation(summary = "Get hot developer deals")
    public ResponseEntity<ApiResponse<List<LandResponse>>> getHotDeveloperDeals(
            @CurrentUser UUID userId,
            @RequestParam(defaultValue = "10") int limit) {
        List<LandResponse> lands = landService.getHotDeveloperDeals(limit, userId);
        return ResponseEntity.ok(ApiResponse.success(lands));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LandResponse>> updateLand(
            @CurrentUser UUID userId,
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody LandCreateRequest request) {
        boolean isAdmin = principal != null && principal.getRole() == com.landgo.coreservice.enums.Role.ADMIN;
        LandResponse land = landService.updateLand(id, request, userId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Land listing updated successfully", land));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLand(
            @CurrentUser UUID userId,
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        boolean isAdmin = principal != null && principal.getRole() == com.landgo.coreservice.enums.Role.ADMIN;
        landService.deleteLand(id, userId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Land listing deleted successfully", null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LandResponse>> updateLandStatus(
            @PathVariable UUID id,
            @RequestParam LandStatus status) {
        LandResponse land = landService.updateLandStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Land status updated", land));
    }

    /**
     * Admin endpoint: returns ALL listings across all statuses (PENDING_APPROVAL, ACTIVE, REJECTED, etc.).
     * Optionally filter by a specific status via ?status=PENDING_APPROVAL
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<LandResponse>>> getAllListingsForAdmin(
            @RequestParam(required = false) LandStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<LandResponse> lands = landService.getAllListingsForAdmin(status, page, size);
        return ResponseEntity.ok(ApiResponse.success(lands));
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleFavorite(
            @CurrentUser UUID userId,
            @PathVariable UUID id) {
        boolean favorited = landService.toggleFavorite(userId, id);
        return ResponseEntity.ok(ApiResponse.success(Map.of("favorited", favorited)));
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<ApiResponse<Map<String, String>>> incrementView(
            @PathVariable UUID id) {
        landService.incrementViewCount(id);
        return ResponseEntity.ok(ApiResponse.success(Map.of("views", "incremented")));
    }

    @PostMapping("/{id}/enquiry")
    public ResponseEntity<ApiResponse<Void>> sendEnquiry(
            @PathVariable UUID id,
            @Valid @RequestBody EnquiryRequest request) {
        enquiryService.createEnquiry(id, request.getSenderNameOrEmail(), request.getSenderPhone(), request.getMessage());
        return ResponseEntity.ok(ApiResponse.success("Enquiry sent successfully", null));
    }
}
