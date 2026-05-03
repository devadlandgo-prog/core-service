package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.response.ApiResponse;
import com.landgo.coreservice.dto.response.PageResponse;
import com.landgo.coreservice.dto.response.VendorResponse;
import com.landgo.coreservice.security.CurrentUser;
import com.landgo.coreservice.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/professionals")
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

    @GetMapping("/subscription-plans")
    public ResponseEntity<ApiResponse<java.util.List<Map<String, Object>>>> getSubscriptionPlans() {
        java.util.List<Map<String, Object>> plans = java.util.List.of(
                Map.of(
                        "id", "basic",
                        "name", "Basic",
                        "description", "Starter plan for professionals",
                        "monthlyPrice", java.math.BigDecimal.valueOf(19.99),
                        "annualPrice", java.math.BigDecimal.valueOf(199.99),
                        "currency", "USD",
                        "features", java.util.List.of("Standard listing exposure"),
                        "isPopular", false
                ),
                Map.of(
                        "id", "professional",
                        "name", "Professional",
                        "description", "Growth plan for active professionals",
                        "monthlyPrice", java.math.BigDecimal.valueOf(49.99),
                        "annualPrice", java.math.BigDecimal.valueOf(499.99),
                        "currency", "USD",
                        "features", java.util.List.of("Priority placement", "Lead insights"),
                        "isPopular", true
                ),
                Map.of(
                        "id", "enterprise",
                        "name", "Enterprise",
                        "description", "Advanced plan for teams",
                        "monthlyPrice", java.math.BigDecimal.valueOf(99.99),
                        "annualPrice", java.math.BigDecimal.valueOf(999.99),
                        "currency", "USD",
                        "features", java.util.List.of("Dedicated support", "Advanced analytics"),
                        "isPopular", false
                )
        );
        return ResponseEntity.ok(ApiResponse.success(plans));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<VendorResponse>> registerProfessional(@CurrentUser UUID userId) {
        VendorResponse vendor = vendorService.getMyVendorProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Professional profile ready", vendor));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<Map<String, String>>> subscribeProfessional() {
        Map<String, String> payload = Map.of(
                "paymentIntentId", "pi_placeholder",
                "clientSecret", "cs_placeholder"
        );
        return ResponseEntity.ok(ApiResponse.success(payload));
    }

    @GetMapping("/me/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(@CurrentUser UUID userId) {
        Map<String, Object> dashboard = Map.of(
                "stats", Map.of("inquiries", 12, "profileViews", 450, "totalProjects", 8),
                "recentInquiries", java.util.List.of()
        );
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @PostMapping("/inquiry")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendInquiry(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(ApiResponse.success(Map.of("inquiryId", UUID.randomUUID().toString())));
    }

    @GetMapping({"/expertise-options", "/expertise"})
    public ResponseEntity<ApiResponse<java.util.List<String>>> getExpertiseOptions() {
        return ResponseEntity.ok(ApiResponse.success(java.util.List.of("Land Surveying", "Architecture", "Legal Advice")));
    }

    @PatchMapping("/me/expertise")
    public ResponseEntity<ApiResponse<java.util.List<String>>> updateExpertise(
            @CurrentUser UUID userId,
            @RequestBody java.util.List<String> expertise) {
        return ResponseEntity.ok(ApiResponse.success("Expertise updated", expertise));
    }
}
