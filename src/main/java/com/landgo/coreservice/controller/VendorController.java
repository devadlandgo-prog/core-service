package com.landgo.coreservice.controller;

import com.landgo.coreservice.security.CurrentUser;
import com.landgo.coreservice.service.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
        // Call user-service to create vendor profile
        try {
            Object response = userServiceClient.createVendorProfile(userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to register professional for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Failed to register professional"));
        }
    }
}
