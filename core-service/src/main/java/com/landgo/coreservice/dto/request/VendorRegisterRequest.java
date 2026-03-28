package com.landgo.coreservice.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class VendorRegisterRequest {
    @NotBlank @Size(min = 2, max = 100) private String companyName;
    private String companyDescription;
    private String companyLogo;
    private String businessLicense;
    @NotBlank private String businessAddress;
    @NotBlank private String businessCity;
    @NotBlank private String businessState;
    @NotBlank private String businessZipCode;
    @NotBlank private String businessCountry;
    private String website;
}
