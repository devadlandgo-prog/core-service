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

    /**
     * MIME type the client will send on the presigned PUT.
     *
     * <p>Matched case-insensitively: browsers and React Native are inconsistent about casing, and
     * a case-only mismatch used to fail the upload with a validation error that named the
     * content type without saying why it was wrong.
     *
     * <p>HEIC/HEIF are accepted because that is what an iPhone camera produces by default — every
     * such upload was rejected before. GIF is accepted alongside the other still formats.
     */
    @NotBlank(message = "Content type is required")
    @Pattern(
            regexp = "(?i)^(image/jpeg|image/jpg|image/pjpeg|image/png|image/webp|image/gif"
                    + "|image/heic|image/heif|image/heic-sequence|image/heif-sequence"
                    + "|application/pdf|application/msword"
                    + "|application/vnd\\.openxmlformats-officedocument\\.wordprocessingml\\.document)$",
            message = "Content type must be one of: image/jpeg, image/jpg, image/png, image/webp, image/gif, "
                    + "image/heic, image/heif, application/pdf, application/msword, "
                    + "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    private String contentType;

    private String context; // e.g., "listings", "profiles"
}
