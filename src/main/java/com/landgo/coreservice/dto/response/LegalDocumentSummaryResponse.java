package com.landgo.coreservice.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * One row in the legal document index.
 *
 * <p>Deliberately omits {@code contentHtml}. A listing exists to populate an admin table or a
 * footer of links, and every document body is a full TEXT column — returning them all would make
 * the payload grow without bound as document types are added. Callers that need the body fetch
 * the single document by type.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegalDocumentSummaryResponse {
    private String documentType;
    private String title;
    private String locale;
    private String version;
    private LocalDateTime updatedAt;
    private List<String> aliases;

    /**
     * True for types backing public pages ({@code privacy}, {@code terms}). DELETE on these
     * returns 403, so the admin UI should render the delete control disabled rather than let
     * the user discover the restriction by hitting an error.
     */
    private boolean protectedDocument;
}
