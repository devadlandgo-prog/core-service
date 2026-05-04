package com.landgo.coreservice.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.landgo.coreservice.dto.request.LandCreateRequest;
import com.landgo.coreservice.dto.response.LandResponse;
import com.landgo.coreservice.entity.Land;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class LandMapper {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LandResponse toResponse(Land land) {
        if (land == null) return null;
        LandResponse.LandResponseBuilder builder = LandResponse.builder()
                .id(land.getId()).projectStage(land.getProjectStage()).status(land.getStatus())
                .address(land.getAddress()).city(land.getCity()).postalCode(land.getPostalCode())
                .lotSize(land.getLotSize()).lotUnit(land.getLotUnit()).frontage(land.getFrontage())
                .depth(land.getDepth()).currentZoningCodes(land.getCurrentZoningCodes())
                .pinNumber(land.getPinNumber()).officialPlanDesignation(land.getOfficialPlanDesignation())
                .latitude(land.getLatitude()).longitude(land.getLongitude())
                .projectSpecification(land.getProjectSpecification())
                .askingPrice(land.getAskingPrice()).currency(land.getCurrency())
                .pricingDescription(land.getPricingDescription())
                .photos(land.getPhotos()).documents(land.getDocuments())
                .ownershipVerification(land.getOwnershipVerification())
                .viewCount(land.getViewCount()).inquiryCount(land.getInquiryCount())
                .isFeatured(land.isFeatured()).isHotDeal(land.isHotDeal())
                .createdAt(land.getCreatedAt()).updatedAt(land.getUpdatedAt());
        
        if (land.getVendorId() != null) {
            builder.vendorId(land.getVendorId());
        }
        return builder.build();
    }

    public Land toEntity(LandCreateRequest request) {
        if (request == null) return null;
        Land land = new Land();
        land.setProjectStage(request.getProjectStage());
        if (request.getPricing() != null) {
            land.setAskingPrice(request.getPricing().getAskingPrice());
            land.setCurrency(request.getPricing().getCurrency());
            land.setPricingDescription(request.getPricing().getDescription());
        } else {
            land.setAskingPrice(request.getAskingPrice());
            land.setCurrency(request.getCurrency());
            land.setPricingDescription(request.getDescription());
        }
        land.setOwnershipVerification(request.getOwnershipVerification());
        land.setViewCount(0); land.setInquiryCount(0);
        LandCreateRequest.ProjectDetailsDto details = request.getProjectDetails();
        if (details == null && request.getAddress() != null) {
            details = LandCreateRequest.ProjectDetailsDto.builder()
                    .address(request.getAddress())
                    .city(request.getCity())
                    .postalCode(request.getPostalCode())
                    .lotSize(request.getLotSize())
                    .lotUnit(request.getLotUnit())
                    .currentZoningCodes(request.getZoning())
                    .coordinates(request.getCoordinates())
                    .build();
        }
        if (details != null) {
            land.setAddress(details.getAddress()); land.setCity(details.getCity());
            land.setPostalCode(details.getPostalCode()); land.setLotSize(details.getLotSize());
            land.setLotUnit(details.getLotUnit()); land.setFrontage(details.getFrontage());
            land.setDepth(details.getDepth()); land.setCurrentZoningCodes(details.getCurrentZoningCodes());
            land.setPinNumber(details.getPinNumber()); land.setOfficialPlanDesignation(details.getOfficialPlanDesignation());
            if (details.getCoordinates() != null) { land.setLatitude(details.getCoordinates().getLat()); land.setLongitude(details.getCoordinates().getLng()); }
        }
        if (request.getProjectSpecification() != null) {
            @SuppressWarnings("unchecked") Map<String, Object> specMap = objectMapper.convertValue(request.getProjectSpecification(), Map.class);
            specMap.values().removeIf(Objects::isNull); land.setProjectSpecification(specMap);
        } else {
            Map<String, Object> specMap = new LinkedHashMap<>();
            if (request.getUtilities() != null) specMap.put("utilities", request.getUtilities());
            if (request.getPropertyTax() != null) specMap.put("propertyTax", request.getPropertyTax());
            if (request.getTitle() != null) specMap.put("title", request.getTitle());
            if (!specMap.isEmpty()) land.setProjectSpecification(specMap);
        }
        if (request.getPhotos() != null) { land.setPhotos(request.getPhotos().stream().map(f -> { Map<String, String> m = new LinkedHashMap<>(); m.put("name", f.getName()); m.put("type", f.getType()); m.put("url", f.getUrl()); return m; }).collect(Collectors.toList())); }
        if (request.getDocuments() != null) { land.setDocuments(request.getDocuments().stream().map(f -> { Map<String, String> m = new LinkedHashMap<>(); m.put("name", f.getName()); m.put("type", f.getType()); m.put("url", f.getUrl()); return m; }).collect(Collectors.toList())); }
        return land;
    }

    public void updateEntity(LandCreateRequest request, Land land) {
        if (request == null || land == null) return;
        land.setProjectStage(request.getProjectStage());
        LandCreateRequest.ProjectDetailsDto details = request.getProjectDetails();
        if (details == null && request.getAddress() != null) {
            details = LandCreateRequest.ProjectDetailsDto.builder()
                    .address(request.getAddress())
                    .city(request.getCity())
                    .postalCode(request.getPostalCode())
                    .lotSize(request.getLotSize())
                    .lotUnit(request.getLotUnit())
                    .currentZoningCodes(request.getZoning())
                    .coordinates(request.getCoordinates())
                    .build();
        }
        if (details != null) {
            land.setAddress(details.getAddress()); land.setCity(details.getCity());
            land.setPostalCode(details.getPostalCode()); land.setLotSize(details.getLotSize());
            land.setLotUnit(details.getLotUnit()); land.setFrontage(details.getFrontage());
            land.setDepth(details.getDepth()); land.setCurrentZoningCodes(details.getCurrentZoningCodes());
            land.setPinNumber(details.getPinNumber()); land.setOfficialPlanDesignation(details.getOfficialPlanDesignation());
            if (details.getCoordinates() != null) { land.setLatitude(details.getCoordinates().getLat()); land.setLongitude(details.getCoordinates().getLng()); }
        }
        if (request.getProjectSpecification() != null) {
            @SuppressWarnings("unchecked") Map<String, Object> specMap = objectMapper.convertValue(request.getProjectSpecification(), Map.class);
            specMap.values().removeIf(Objects::isNull); land.setProjectSpecification(specMap);
        }
        if (request.getPricing() != null) {
            land.setAskingPrice(request.getPricing().getAskingPrice());
            land.setCurrency(request.getPricing().getCurrency());
            land.setPricingDescription(request.getPricing().getDescription());
        } else if (request.getAskingPrice() != null || request.getCurrency() != null || request.getDescription() != null) {
            if (request.getAskingPrice() != null) land.setAskingPrice(request.getAskingPrice());
            if (request.getCurrency() != null) land.setCurrency(request.getCurrency());
            if (request.getDescription() != null) land.setPricingDescription(request.getDescription());
        }
        if (request.getPhotos() != null) { land.setPhotos(request.getPhotos().stream().map(f -> { Map<String, String> m = new LinkedHashMap<>(); m.put("name", f.getName()); m.put("type", f.getType()); m.put("url", f.getUrl()); return m; }).collect(Collectors.toList())); }
        if (request.getDocuments() != null) { land.setDocuments(request.getDocuments().stream().map(f -> { Map<String, String> m = new LinkedHashMap<>(); m.put("name", f.getName()); m.put("type", f.getType()); m.put("url", f.getUrl()); return m; }).collect(Collectors.toList())); }
        land.setOwnershipVerification(request.getOwnershipVerification());
    }
}
