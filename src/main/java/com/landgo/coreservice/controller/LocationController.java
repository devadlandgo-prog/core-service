package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Locations", description = "Country, province/state, and city reference data")
public class LocationController {

    private final com.landgo.coreservice.repository.LocationRepository locationRepository;

    @GetMapping
    @Operation(summary = "Get all locations")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getAllLocations() {
        List<Map<String, String>> locations = locationRepository.findAll().stream()
                .map(l -> Map.of("id", l.getId().toString(), "name", l.getName(), "code", l.getCode(), "type", l.getType()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(locations));
    }

    @GetMapping("/countries")
    @Operation(summary = "Get all active countries")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getCountries() {
        List<Map<String, String>> countries = locationRepository.findByTypeAndIsActiveTrue("COUNTRY").stream()
                .map(l -> Map.of("code", l.getCode(), "name", l.getName()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(countries));
    }

    @GetMapping("/states")
    @Operation(summary = "Get provinces/states for a country", description = "Pass ?countryCode=CA to get Canadian provinces.")
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
    @Operation(summary = "Get cities for a province/state", description = "Pass ?stateCode=ON to get cities in Ontario.")
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
    @Operation(summary = "Get location filter options", description = "Returns property types, project stages, and lot units for filter dropdowns.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFilterOptions() {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "propertyTypes", List.of("Residential", "Commercial", "Industrial", "Mixed Use"),
                "projectStages", List.of("Raw Land", "Zoned", "Draft Plan Approved", "Site Plan Approved"),
                "lotUnits", List.of("SQFT", "ACRE", "HECTARE")
        )));
    }
}
