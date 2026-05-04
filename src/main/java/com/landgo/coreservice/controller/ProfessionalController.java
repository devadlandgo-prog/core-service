package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.request.ProfessionalVerificationRequest;
import com.landgo.coreservice.dto.response.ApiResponse;
import com.landgo.coreservice.service.LandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/professionals")
@RequiredArgsConstructor
public class ProfessionalController {

    private final LandService landService;

    @PatchMapping("/{id}/verify")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> verifyProfessional(
            @PathVariable UUID id,
            @Valid @RequestBody ProfessionalVerificationRequest request) {
        landService.updateProfessionalVerificationStatus(id, request.getStatus(), request.getNotes());
        return ResponseEntity.ok(ApiResponse.success("Professional verification status updated", null));
    }
}
