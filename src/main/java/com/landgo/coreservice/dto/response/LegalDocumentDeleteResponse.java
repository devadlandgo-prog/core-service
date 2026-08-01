package com.landgo.coreservice.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegalDocumentDeleteResponse {
    private String documentType;
    private boolean deleted;
}
