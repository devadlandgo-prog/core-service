package com.landgo.coreservice.dto.request;
import com.landgo.coreservice.enums.ProjectStage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LandCreateRequest {
    @NotNull(message = "Project stage is required") private ProjectStage projectStage;
    @Valid private ProjectDetailsDto projectDetails;
    @Valid private ProjectSpecificationDto projectSpecification;
    @Valid private PricingDto pricing;
    @Size(max = 10) private List<FileDto> photos;
    @Size(max = 25) private List<FileDto> documents;
    private String ownershipVerification;
    private Boolean isAdmin;

    // Flat payload compatibility fields (DOCX contract style)
    private String title;
    private String address;
    private String city;
    private String postalCode;
    private CoordinatesDto coordinates;
    private BigDecimal lotSize;
    private String lotUnit;
    private BigDecimal askingPrice;
    private String currency;
    private String description;
    private String zoning;
    private BigDecimal propertyTax;
    private List<String> utilities;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProjectDetailsDto {
        @NotBlank private String address;
        @NotBlank private String city;
        @NotBlank private String postalCode;
        @NotNull @Positive private BigDecimal lotSize;
        @NotBlank private String lotUnit;
        private String frontage; private String depth; private String currentZoningCodes;
        private String pinNumber; private String officialPlanDesignation;
        @Valid private CoordinatesDto coordinates;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CoordinatesDto { private BigDecimal lat; private BigDecimal lng; }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProjectSpecificationDto {
        private String buildingType; private String proposedUse; private String sitePlanStatus;
        private String subdivisionType; private String lotBlockType; private String draftPlanStatus;
        @Valid private ServicesDto services;
        @Valid private ProposedDevelopmentTypeDto proposedDevelopmentType;
        private String submissionStatus; private String projectType; private String sellingType;
        private String constructionStartTimeline; private String approvalStatus;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ServicesDto { private Boolean gas; private Boolean hydro; private Boolean municipalSewer; private Boolean municipalWater; private Boolean septic; private Boolean well; private Boolean none; }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProposedDevelopmentTypeDto { private Boolean rezoning; private Boolean sitePlan; private Boolean subdivision; }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PricingDto {
        @NotNull @DecimalMin(value = "0.0", inclusive = false) private BigDecimal askingPrice;
        @NotBlank private String currency;
        private String description;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class FileDto { @NotBlank private String name; private String type; @NotBlank private String url; }
}
