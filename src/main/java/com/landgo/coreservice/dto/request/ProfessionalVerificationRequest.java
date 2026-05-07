package com.landgo.coreservice.dto.request;

import com.landgo.coreservice.enums.VerificationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalVerificationRequest {
    @NotNull(message = "status is required")
    private VerificationStatus status;

    @Size(max = 500, message = "notes must be at most 500 characters")
    private String notes;
}
