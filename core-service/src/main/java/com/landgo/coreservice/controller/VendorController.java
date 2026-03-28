package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.response.ApiResponse;
import com.landgo.coreservice.dto.response.PageResponse;
import com.landgo.coreservice.dto.response.VendorResponse;
import com.landgo.coreservice.security.CurrentUser;
import com.landgo.coreservice.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VendorResponse>> getVendorById(
            @PathVariable UUID id,
            @CurrentUser UUID userId) {
        VendorResponse vendor = vendorService.getVendorById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(vendor));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<VendorResponse>> getMyVendorProfile(@CurrentUser UUID userId) {
        VendorResponse vendor = vendorService.getMyVendorProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(vendor));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<VendorResponse>>> getVerifiedVendors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<VendorResponse> vendors = vendorService.getVerifiedVendors(page, size);
        return ResponseEntity.ok(ApiResponse.success(vendors));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<VendorResponse>>> searchVendors(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<VendorResponse> vendors = vendorService.searchVendors(q, page, size);
        return ResponseEntity.ok(ApiResponse.success(vendors));
    }
}
