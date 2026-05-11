package com.landgo.coreservice.dto.response;
import com.landgo.coreservice.enums.LandStatus;
import com.landgo.coreservice.enums.ProjectStage;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Response object containing detailed land listing information")
public class LandResponse {
    @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    
    @Schema(example = "Prime Development Land in Toronto")
    private String title;
    
    @Schema(example = "RAW_LAND")
    private ProjectStage projectStage;
    
    @Schema(example = "ACTIVE")
    private LandStatus status;
    
    @Schema(example = "123 Main St")
    private String address;
    
    @Schema(example = "Toronto")
    private String city;
    
    @Schema(example = "M5V 1J2")
    private String postalCode;
    
    @Schema(example = "1.5")
    private BigDecimal lotSize;
    
    @Schema(example = "Acres")
    private String lotUnit;
    
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
    
    @Schema(example = "43.6532")
    private BigDecimal latitude;
    
    @Schema(example = "-79.3832")
    private BigDecimal longitude;
    
    private Map<String, Object> projectSpecification;
    
    @Schema(example = "1250000.00")
    private BigDecimal askingPrice;
    
    @Schema(example = "CAD")
    private String currency;
    
    @Schema(example = "Excellent opportunity for multi-residential development.")
    private String pricingDescription;
    
    private List<Map<String, String>> photos;
    private List<Map<String, String>> documents;
    
    @Schema(example = "Verified by LandGo team")
    private String ownershipVerification;
    
    @Schema(example = "150")
    private Integer viewCount;
    
    @Schema(example = "5")
    private Integer inquiryCount;
    
    @Schema(example = "true")
    private boolean isFeatured;
    
    @Schema(example = "false")
    private boolean isHotDeal;
    
    @Schema(example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID vendorId;
    
    @Schema(example = "Acme Developers Inc.")
    private String vendorCompanyName;
    
    @Schema(example = "true")
    private boolean vendorVerified;
    
    @Schema(example = "John Doe")
    private String vendorOwnerName;
    
    @Schema(example = "john@acme.com")
    private String vendorOwnerEmail;
    
    @Schema(example = "4.8")
    private BigDecimal vendorRating;
    
    @Schema(example = "25")
    private Integer vendorTotalReviews;
    
    @Schema(example = "10")
    private Integer vendorYearsOfExperience;
    
    @Schema(example = "false")
    private boolean isFavorited;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
