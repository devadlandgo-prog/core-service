package com.landgo.coreservice.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegalDocumentResponse {
    private String documentType;
    private String title;
    private String locale;
    private String version;
    private LocalDateTime updatedAt;
    private String contentHtml;
    private List<String> aliases;
}
