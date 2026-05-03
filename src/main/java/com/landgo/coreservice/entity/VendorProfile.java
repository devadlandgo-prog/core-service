package com.landgo.coreservice.entity;

import com.landgo.coreservice.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;

@Entity @Table(name = "vendor_profiles")
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class VendorProfile extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false, unique = true) private User user;
    @Column(name = "company_name", nullable = false, length = 150) private String companyName;
    @Column(name = "company_description", columnDefinition = "TEXT") private String companyDescription;
    @Column(name = "company_logo", length = 2000) private String companyLogo;
    @Column(name = "license_number", length = 50) private String licenseNumber;
    @ElementCollection @CollectionTable(name = "vendor_specializations", joinColumns = @JoinColumn(name = "vendor_id")) @Column(name = "specialization") private java.util.List<String> specialization;
    @Column(name = "years_of_experience") private Integer yearsOfExperience;
    @Column(name = "phone_number", length = 20) private String phoneNumber;
    @ElementCollection @CollectionTable(name = "vendor_service_areas", joinColumns = @JoinColumn(name = "vendor_id")) @Column(name = "service_area") private java.util.List<String> serviceArea;
    @Column(name = "bio", columnDefinition = "TEXT") private String bio;
    @ElementCollection @CollectionTable(name = "vendor_certifications", joinColumns = @JoinColumn(name = "vendor_id")) @Column(name = "certification") private java.util.List<String> certifications;
    @Column(name = "business_address", nullable = false) private String businessAddress;
    @Column(name = "business_city", nullable = false, length = 100) private String businessCity;
    @Column(name = "business_state", nullable = false, length = 100) private String businessState;
    @Column(name = "business_zip_code", nullable = false, length = 20) private String businessZipCode;
    @Column(name = "business_country", nullable = false, length = 100) private String businessCountry;
    @Column(name = "website") private String website;
    @Column(name = "verified") @Builder.Default private boolean verified = false;
    @Column(name = "verification_notes", length = 500) private String verificationNotes;
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", length = 20, nullable = false)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
    @Column(name = "rating", precision = 3, scale = 2) private BigDecimal rating;
    @Column(name = "total_reviews") @Builder.Default private Integer totalReviews = 0;
    @Column(name = "total_lands_listed") @Builder.Default private Integer totalLandsListed = 0;
    @Column(name = "total_lands_sold") @Builder.Default private Integer totalLandsSold = 0;
    public void incrementLandsListed() { this.totalLandsListed++; }
    public void incrementLandsSold() { this.totalLandsSold++; }
}
