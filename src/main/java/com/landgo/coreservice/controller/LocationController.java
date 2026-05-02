package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class LocationController {

    @GetMapping("/locations/countries")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getCountries() {
        return ResponseEntity.ok(ApiResponse.success(List.of(Map.of("code", "CA", "name", "Canada"))));
    }

    @GetMapping("/locations/states")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getStates(@RequestParam String countryCode) {
        return ResponseEntity.ok(ApiResponse.success(List.of(Map.of("code", "ON", "name", "Ontario"))));
    }

    @GetMapping("/locations/cities")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getCities(@RequestParam String stateCode) {
        return ResponseEntity.ok(ApiResponse.success(List.of(Map.of("code", "TOR", "name", "Toronto"))));
    }

    @GetMapping("/filter-options")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFilterOptions() {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "propertyTypes", List.of("Residential", "Commercial", "Industrial"),
                "statuses", List.of("For Sale", "Lease"),
                "amenities", List.of("Water", "Electricity", "Road Access")
        )));
    }
}
