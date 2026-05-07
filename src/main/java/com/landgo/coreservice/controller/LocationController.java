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
@RequestMapping("/locations")
@RequiredArgsConstructor
public class LocationController {

    private final com.landgo.coreservice.repository.LocationRepository locationRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getAllLocations() {
        List<Map<String, String>> locations = locationRepository.findAll().stream()
                .map(l -> Map.of("id", l.getId().toString(), "name", l.getName(), "code", l.getCode(), "type", l.getType()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(locations));
    }

    @GetMapping("/countries")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getCountries() {
        List<Map<String, String>> countries = locationRepository.findByTypeAndIsActiveTrue("COUNTRY").stream()
                .map(l -> Map.of("code", l.getCode(), "name", l.getName()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(countries));
    }

    @GetMapping("/states")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getStates(@RequestParam String countryCode) {
        return locationRepository.findByCodeAndTypeAndIsActiveTrue(countryCode, "COUNTRY")
                .map(country -> {
                    List<Map<String, String>> states = locationRepository.findByParentIdAndIsActiveTrue(country.getId()).stream()
                            .map(l -> Map.of("code", l.getCode(), "name", l.getName()))
                            .toList();
                    return ResponseEntity.ok(ApiResponse.success(states));
                })
                .orElse(ResponseEntity.ok(ApiResponse.success(List.of())));
    }

    @GetMapping("/cities")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getCities(@RequestParam String stateCode) {
        return locationRepository.findByCodeAndTypeAndIsActiveTrue(stateCode, "STATE")
                .map(state -> {
                    List<Map<String, String>> cities = locationRepository.findByParentIdAndIsActiveTrue(state.getId()).stream()
                            .map(l -> Map.of("code", l.getCode(), "name", l.getName()))
                            .toList();
                    return ResponseEntity.ok(ApiResponse.success(cities));
                })
                .orElse(ResponseEntity.ok(ApiResponse.success(List.of())));
    }

    @GetMapping("/filter-options")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFilterOptions() {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "propertyTypes", List.of("Residential", "Commercial", "Industrial", "Mixed Use"),
                "projectStages", List.of("Raw Land", "Zoned", "Draft Plan Approved", "Site Plan Approved"),
                "lotUnits", List.of("SQFT", "ACRE", "HECTARE")
        )));
    }
}
