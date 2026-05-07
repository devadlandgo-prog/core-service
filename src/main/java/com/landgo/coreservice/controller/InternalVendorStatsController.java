package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.response.VendorListingStatsResponse;
import com.landgo.coreservice.service.VendorListingStatsService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Hidden
@RestController
@RequestMapping("/internal/vendors")
@RequiredArgsConstructor
public class InternalVendorStatsController {

    private final VendorListingStatsService vendorListingStatsService;

    @GetMapping("/{vendorId}/listing-stats")
    public ResponseEntity<VendorListingStatsResponse> listingStats(@PathVariable UUID vendorId) {
        return ResponseEntity.ok(vendorListingStatsService.getStatsForVendor(vendorId));
    }
}
