package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.response.ApiResponse;
import com.landgo.coreservice.entity.Enquiry;
import com.landgo.coreservice.enums.EnquiryStatus;
import com.landgo.coreservice.service.EnquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/enquiries")
@RequiredArgsConstructor
public class EnquiryController {

    private final EnquiryService enquiryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Enquiry>>> getAllEnquiries() {
        return ResponseEntity.ok(ApiResponse.success(enquiryService.getAllEnquiries()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Enquiry>> getEnquiryById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(enquiryService.getEnquiryById(id)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateEnquiryStatus(
            @PathVariable UUID id,
            @RequestParam EnquiryStatus status) {
        enquiryService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Enquiry status updated", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnquiry(@PathVariable UUID id) {
        enquiryService.deleteEnquiry(id);
        return ResponseEntity.noContent().build();
    }
}
