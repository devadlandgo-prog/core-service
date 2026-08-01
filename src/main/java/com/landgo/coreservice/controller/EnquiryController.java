package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.response.ApiResponse;
import com.landgo.coreservice.entity.Enquiry;
import com.landgo.coreservice.enums.EnquiryStatus;
import com.landgo.coreservice.service.EnquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/enquiries")
@RequiredArgsConstructor
@Tag(name = "Enquiries", description = "Manage buyer enquiries submitted on listings (admin)")
public class EnquiryController {

    private final EnquiryService enquiryService;

    @GetMapping
    @Operation(summary = "List all enquiries (admin)")
    public ResponseEntity<ApiResponse<List<Enquiry>>> getAllEnquiries() {
        return ResponseEntity.ok(ApiResponse.success(enquiryService.getAllEnquiries()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get enquiry by ID (admin)")
    public ResponseEntity<ApiResponse<Enquiry>> getEnquiryById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(enquiryService.getEnquiryById(id)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update enquiry status (admin)", description = "Transitions the enquiry to a new status (e.g. OPEN, RESOLVED, CLOSED).")
    public ResponseEntity<ApiResponse<Void>> updateEnquiryStatus(
            @PathVariable UUID id,
            @RequestParam EnquiryStatus status) {
        enquiryService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Enquiry status updated", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an enquiry (admin)")
    public ResponseEntity<Void> deleteEnquiry(@PathVariable UUID id) {
        enquiryService.deleteEnquiry(id);
        return ResponseEntity.noContent().build();
    }
}
