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
public class ImageConfirmRequest {
    @NotBlank(message = "File key is required")
    private String fileKey;
    
    @NotBlank(message = "Original file name is required")
    private String fileName;
    
    private boolean isPrimary;
}
