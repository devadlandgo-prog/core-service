package com.landgo.coreservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignedReadUrlRequest {

    @NotBlank(message = "fileKey is required")
    private String fileKey;

    /**
     * Optional expiry in minutes. Defaults to 60 if not provided.
     * Max allowed: 720 (12 hours).
     */
    private Integer expiryMinutes;
}
