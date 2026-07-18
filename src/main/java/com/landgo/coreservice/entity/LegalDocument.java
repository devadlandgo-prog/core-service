package com.landgo.coreservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
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
}
