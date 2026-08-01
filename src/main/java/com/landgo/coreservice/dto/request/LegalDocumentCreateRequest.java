package com.landgo.coreservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegalDocumentCreateRequest {

    @NotBlank(message = "Document type is required")
    @Size(min = 3, max = 64, message = "Document type must be between 3 and 64 characters")
    @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$",
            message = "Document type must be lowercase kebab-case (letters, digits and single hyphens)")
    private String documentType;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Locale is required")
    private String locale;

    @NotBlank(message = "Content HTML is required")
    private String contentHtml;

    /** Optional; defaults to today as YYYY-MM-DD. */
    private String version;

    /** Optional alternate slugs the document can also be fetched by. */
    private List<String> aliases;
}
