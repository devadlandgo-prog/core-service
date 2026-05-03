package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.response.ApiResponse;
import com.landgo.coreservice.enums.VerificationStatus;
import com.landgo.coreservice.service.LandService;
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
    public ResponseEntity<ApiResponse<Void>> verifyProfessional(
            @PathVariable UUID id,
            @RequestParam VerificationStatus status) {
        landService.updateProfessionalVerificationStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Professional verification status updated", null));
    }
}
