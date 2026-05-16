package com.landgo.coreservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadRequest {
    @NotBlank(message = "File name is required")
    private String fileName;

    @NotBlank(message = "Content type is required")
    @Pattern(regexp = "^(image/jpeg|image/jpg|image/png|image/webp|application/pdf|application/msword|application/vnd.openxmlformats-officedocument.wordprocessingml.document)$",
            message = "Content type must be one of: image/jpeg, image/jpg, image/png, image/webp, application/pdf, application/msword, application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    private String contentType;

    private String context; // e.g., "listings", "profiles"
}
