package com.landgo.coreservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegalDocumentRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Locale is required")
    private String locale;

    private String version;

    @NotBlank(message = "Content HTML is required")
    private String contentHtml;
}
