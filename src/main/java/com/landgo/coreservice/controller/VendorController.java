package com.landgo.coreservice.controller;

import com.landgo.coreservice.security.CurrentUser;
import com.landgo.coreservice.service.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/professionals")
@RequiredArgsConstructor
public class VendorController {

    private final UserServiceClient userServiceClient;

    @GetMapping("/expertise-options")
    public ResponseEntity<?> getExpertiseOptions() {
        return ResponseEntity.ok(new String[]{"Land Surveying", "Architecture", "Legal Advice", "Civil Engineering", "Environmental Assessment", "Urban Planning", "Real Estate Law", "Property Appraisal"});
    }

    @GetMapping
    public ResponseEntity<?> getVerifiedVendors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok("[]");
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchVendors(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok("[]");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVendorById(@PathVariable UUID id) {
        return ResponseEntity.ok(userServiceClient.getVendorProfileForUser(id));
    }

    @GetMapping("/me/dashboard")
    public ResponseEntity<?> getMyDashboard(@CurrentUser UUID userId) {
        return ResponseEntity.ok("{}");
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerProfessional(
            @CurrentUser UUID userId,
            @RequestBody Map<String, Object> request) {
        log.info("Professional registration request for user: {} with data: {}", userId, request);
        try {
            Object response = userServiceClient.createVendorProfile(userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to register professional for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Failed to register professional"));
        }
    }

    // ── Admin APIs ───────────────────────────────────────────────────────────

    /**
     * GET /core/professionals/admin/all?page=0&size=20
     * Returns all professionals (isProfessional=true users), paginated.
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllProfessionalsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Object result = userServiceClient.getAllProfessionals(page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * PUT /core/professionals/admin/{id}
     * Admin updates a professional's profile fields.
     */
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProfessionalForAdmin(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> request) {
        Object result = userServiceClient.updateProfessionalProfile(id, request);
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /core/professionals/admin/{id}
     * Admin deactivates a professional (soft-delete: active=false, isProfessional=false).
     */
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivateProfessionalForAdmin(@PathVariable UUID id) {
        userServiceClient.deactivateProfessional(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Professional deactivated successfully"));
    }
}
