package com.landgo.coreservice.dto.request;
import com.landgo.coreservice.enums.SpecializationType;
import jakarta.validation.constraints.*;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class VendorRegisterRequest {
    @NotBlank @Size(min = 2, max = 150) private String companyName;
    private String companyDescription;
    private String companyLogo;
    @NotBlank @Size(max = 50) private String licenseNumber;
    private java.util.List<String> specialization;
    @Min(0) private Integer yearsOfExperience;
    @NotBlank private String phoneNumber;
    private java.util.List<String> serviceArea;
    private String profileImageUrl;
    @Size(max = 1000) private String bio;
    private java.util.List<String> certifications;
    @NotBlank private String businessAddress;
    @NotBlank private String businessCity;
    @NotBlank private String businessState;
    @NotBlank private String businessZipCode;
    @NotBlank private String businessCountry;
    private String website;
}
