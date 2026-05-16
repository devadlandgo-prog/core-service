package com.landgo.coreservice.dto.request;
import com.landgo.coreservice.enums.ProjectStage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Request object for creating or updating a land listing")
public class LandCreateRequest {
    @Schema(example = "RAW_LAND")
    @NotNull(message = "Project stage is required") private ProjectStage projectStage;
    
    private ProjectDetailsDto projectDetails;
    private ProjectSpecificationDto projectSpecification;
    private PricingDto pricing;
    
    @Size(max = 10) private List<FileDto> photos;
    @Size(max = 25) private List<FileDto> documents;
    
    @Schema(example = "Verified by LandGo team")
    private String ownershipVerification;
    
    @Schema(example = "Passport")
    private String personalUserId;
    
    @Schema(example = "false")
    private Boolean agentManagement;
    
    private Boolean isAdmin;

    // Flat payload compatibility fields (DOCX contract style)
    @Schema(example = "Prime Development Land in Toronto")
    private String title;
    
    @Schema(example = "123 Main St")
    private String address;
    
    @Schema(example = "Toronto")
    private String city;
    
    @Schema(example = "M5V 1J2")
    private String postalCode;
    
    private CoordinatesDto coordinates;
    
    @Schema(example = "1.5")
    private BigDecimal lotSize;
    
    @Schema(example = "Acres")
    private String lotUnit;
    
    @Schema(example = "1250000.00")
    private BigDecimal askingPrice;
    
    @Schema(example = "CAD")
    private String currency;
    
    @Schema(example = "Excellent opportunity for multi-residential development.")
    private String description;
    
    @Schema(example = "R1 - Residential")
    private String zoning;
    
    @Schema(example = "5200.50")
    private BigDecimal propertyTax;
    
    @Schema(example = "[\"Hydro\", \"Water\", \"Gas\"]")
    private List<String> utilities;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @Schema(description = "Detailed address and location of the property")
    public static class ProjectDetailsDto {
        @Schema(example = "123 Main St")
        @NotBlank private String address;
        
        @Schema(example = "Toronto")
        @NotBlank private String city;
        
        @Schema(example = "M5V 1J2")
        @NotBlank private String postalCode;
        
        @Schema(example = "1.5")
        @NotNull @Positive private BigDecimal lotSize;
        
        @Schema(example = "Acres")
        @NotBlank private String lotUnit;
        
        @Schema(example = "50ft")
        private String frontage; 
        
        @Schema(example = "120ft")
        private String depth; 
        
        @Schema(example = "R1")
        private String currentZoningCodes;
        
        @Schema(example = "123-456-789")
        private String pinNumber; 
        
        @Schema(example = "Residential")
        private String officialPlanDesignation;
        
        private CoordinatesDto coordinates;
    }
    
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CoordinatesDto { 
        @Schema(example = "43.6532")
        private BigDecimal lat; 
        
        @Schema(example = "-79.3832")
        private BigDecimal lng; 
    }
    
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProjectSpecificationDto {
        @Schema(example = "Detached")
        private String buildingType; 
        
        @Schema(example = "Residential")
        private String proposedUse; 
        
        @Schema(example = "Approved")
        private String sitePlanStatus;
        
        private String subdivisionType; private String lotBlockType; private String draftPlanStatus;
        @Valid private ServicesDto services;
        @Valid private ProposedDevelopmentTypeDto proposedDevelopmentType;
        private String submissionStatus; private String projectType; private String sellingType;
        private String constructionStartTimeline; private String approvalStatus;
    }
    
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ServicesDto { 
        @Schema(example = "true")
        private Boolean gas; 
        @Schema(example = "true")
        private Boolean hydro; 
        @Schema(example = "true")
        private Boolean municipalSewer; 
        @Schema(example = "true")
        private Boolean municipalWater; 
        private Boolean septic; private Boolean well; private Boolean none; 
    }
    
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProposedDevelopmentTypeDto { 
        private Boolean rezoning; 
        @Schema(example = "true")
        private Boolean sitePlan; 
        private Boolean subdivision; 
    }
    
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PricingDto {
        @Schema(example = "1250000.00")
        @NotNull @DecimalMin(value = "0.0", inclusive = false) private BigDecimal askingPrice;
        
        @Schema(example = "CAD")
        @NotBlank private String currency;
        
        @Schema(example = "Negotiable")
        private String description;
    }
    
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class FileDto { 
        @Schema(example = "main-photo.jpg")
        @NotBlank private String name; 
        
        @Schema(example = "image/jpeg")
        private String type; 
        
        @Schema(example = "https://landgo-images.s3.amazonaws.com/main-photo.jpg")
        @NotBlank private String url; 
    }
}
