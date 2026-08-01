package com.landgo.coreservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "legal_documents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegalDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "document_type", nullable = false, unique = true, length = 50)
    private String documentType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "locale", nullable = false, length = 20)
    private String locale;

    @Column(name = "version", length = 50)
    private String version;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "content_html", nullable = false, columnDefinition = "TEXT")
    private String contentHtml;

    /** Alternate slugs this document also resolves under, e.g. "privacy-policy" for "privacy". */
    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "aliases", columnDefinition = "jsonb")
    private List<String> aliases = new ArrayList<>();

    /** Protected documents back public web pages and cannot be deleted. */
    @Builder.Default
    @Column(name = "protected", nullable = false)
    private boolean protectedDocument = false;
}
