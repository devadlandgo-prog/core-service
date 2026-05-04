package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.request.VendorRegisterRequest;
import com.landgo.coreservice.dto.request.ProfessionalSubscriptionRequest;
import com.landgo.coreservice.dto.response.ApiResponse;
import com.landgo.coreservice.dto.response.PageResponse;
import com.landgo.coreservice.dto.response.VendorResponse;
import com.landgo.coreservice.security.CurrentUser;
import com.landgo.coreservice.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<VendorResponse>> registerProfessional(
            @CurrentUser UUID userId,
            @Valid @RequestBody VendorRegisterRequest request) {
        VendorResponse vendor = vendorService.registerProfessional(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Professional profile updated successfully", vendor));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<Map<String, String>>> subscribeProfessional(
            @CurrentUser UUID userId,
            @Valid @RequestBody ProfessionalSubscriptionRequest request) {
        Map<String, String> payload = vendorService.createProfessionalSubscriptionIntent(userId, request);
        return ResponseEntity.ok(ApiResponse.success(payload));
    }

    @GetMapping("/me/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(@CurrentUser UUID userId) {
        Map<String, Object> dashboard = vendorService.getVendorDashboard(userId);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @PostMapping("/inquiry")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendInquiry(@RequestBody Map<String, String> request) {
        UUID inquiryId = vendorService.sendInquiry(request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("inquiryId", inquiryId.toString())));
    }

    // ── Expertise options (managed list) ────────────────────────────────────

    @GetMapping({"/expertise-options", "/expertise"})
    public ResponseEntity<ApiResponse<List<String>>> getExpertiseOptions() {
        return ResponseEntity.ok(ApiResponse.success(vendorService.getExpertiseOptions()));
    }

    @PostMapping("/expertise-options")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createExpertiseOption(
            @RequestBody Map<String, String> request) {
        String name = request.getOrDefault("name", "");
        if (name.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Name is required"));
        }
        com.landgo.coreservice.entity.ExpertiseOption created = vendorService.createExpertiseOption(name);
        return ResponseEntity.ok(ApiResponse.success("Expertise option created",
                Map.of("id", created.getId().toString(), "name", created.getName())));
    }

    @PutMapping("/expertise-options/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateExpertiseOption(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        String name = request.getOrDefault("name", "");
        if (name.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Name is required"));
        }
        vendorService.updateExpertiseOption(id, name);
        return ResponseEntity.ok(ApiResponse.success("Expertise option updated",
                Map.of("id", id.toString(), "name", name)));
    }

    @DeleteMapping("/expertise-options/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpertiseOption(@PathVariable UUID id) {
        vendorService.deleteExpertiseOption(id);
        return ResponseEntity.ok(ApiResponse.success("Expertise option deleted", null));
    }

    @PatchMapping("/me/expertise")
    public ResponseEntity<ApiResponse<List<String>>> updateExpertise(
            @CurrentUser UUID userId,
            @RequestBody List<String> expertise) {
        vendorService.updateVendorExpertise(userId, expertise);
        return ResponseEntity.ok(ApiResponse.success("Expertise updated", expertise));
    }
}
