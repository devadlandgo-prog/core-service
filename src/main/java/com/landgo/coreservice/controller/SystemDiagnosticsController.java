package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.response.ApiResponse;
import com.landgo.coreservice.entity.Enquiry;
import com.landgo.coreservice.repository.EnquiryRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/internal/diagnostics")
@RequiredArgsConstructor
public class SystemDiagnosticsController {

    private final EnquiryRepository enquiryRepository;

    @GetMapping("/write-check")
    @Operation(summary = "Perform a test database write to verify transaction persistence")
    public ResponseEntity<ApiResponse<String>> checkWriteCapability() {
        log.info("Starting diagnostic write check...");
        
        // Create a temporary enquiry
        Enquiry testEnquiry = Enquiry.builder()
                .senderName("DIAGNOSTIC_CHECK")
                .senderEmail("diag@landgo.com")
                .message("Diagnostic test write at " + java.time.LocalDateTime.now())
                .listingId(UUID.randomUUID())
                .build();
        
        try {
            Enquiry saved = enquiryRepository.save(testEnquiry);
            UUID id = saved.getId();
            log.info("Diagnostic write successful. ID: {}", id);
            
            // Immediately delete it to keep DB clean
            enquiryRepository.deleteById(id);
            log.info("Diagnostic cleanup successful.");
            
            return ResponseEntity.ok(ApiResponse.success("Database write capability verified successfully."));
        } catch (Exception e) {
            log.error("Diagnostic write check failed!", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Database write failure: " + e.getMessage(), "DB_WRITE_ERROR"));
        }
    }
}
