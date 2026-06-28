package com.landgo.coreservice.controller;

import com.landgo.coreservice.service.VendorListingStatsService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@Hidden
@RestController
@RequestMapping("/internal/listings")
@RequiredArgsConstructor
public class InternalListingsController {

    private final VendorListingStatsService vendorListingStatsService;

    @GetMapping("/user/{userId}/slots-used")
    public ResponseEntity<Map<String, Long>> getSlotsUsed(@PathVariable UUID userId) {
        long slotsUsed = vendorListingStatsService.getSlotsUsed(userId);
        return ResponseEntity.ok(Map.of("slotsUsed", slotsUsed));
    }

    @org.springframework.web.bind.annotation.PostMapping("/user/{userId}/downgrade")
    public ResponseEntity<Map<String, String>> handleDowngrade(@PathVariable UUID userId) {
        vendorListingStatsService.handleSubscriptionDowngrade(userId);
        return ResponseEntity.ok(Map.of("status", "Downgrade processed"));
    }
}
