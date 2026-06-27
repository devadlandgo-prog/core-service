package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.response.ApiResponse;
import com.landgo.coreservice.security.CurrentUser;
import com.landgo.coreservice.service.LandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/listings")
@RequiredArgsConstructor
@Tag(name = "Listing Slot Usage", description = "Endpoints for checking listing slots and usage")
public class SlotUsageController {

    private final LandService landService;

    @GetMapping("/mine/slot-usage")
    @Operation(summary = "Get listing slot usage details", description = "Returns active, pending, draft, total listing counts and max limit for the current user.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSlotUsage(@CurrentUser UUID userId) {
        Map<String, Object> usage = landService.getSlotUsage(userId);
        return ResponseEntity.ok(ApiResponse.success(usage));
    }
}
