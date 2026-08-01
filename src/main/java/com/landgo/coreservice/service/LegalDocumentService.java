package com.landgo.coreservice.service;

import com.landgo.coreservice.dto.request.LegalDocumentCreateRequest;
import com.landgo.coreservice.dto.request.LegalDocumentRequest;
import com.landgo.coreservice.dto.response.LegalDocumentDeleteResponse;
import com.landgo.coreservice.dto.response.LegalDocumentResponse;
import com.landgo.coreservice.entity.LegalDocument;
import com.landgo.coreservice.exception.ApiException;
import com.landgo.coreservice.exception.BadRequestException;
import com.landgo.coreservice.exception.ConflictException;
import com.landgo.coreservice.repository.LegalDocumentRepository;
import com.landgo.coreservice.util.LegalHtmlSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class LegalDocumentService {

    /** Documents backing public web routes (/privacy, /terms) — these can never be deleted. */
    private static final Set<String> PROTECTED_TYPES = Set.of("privacy", "terms");

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");
    private static final int SLUG_MIN_LENGTH = 3;
    private static final int SLUG_MAX_LENGTH = 64;

    private final LegalDocumentRepository legalDocumentRepository;

    @Transactional(readOnly = true)
    public LegalDocumentResponse getLegalDocument(String documentType) {
        return toResponse(requireDocument(documentType));
    }

    @Transactional
    public LegalDocumentResponse createLegalDocument(LegalDocumentCreateRequest request) {
        String documentType = normalize(request.getDocumentType());
        validateSlug(documentType);

        List<String> aliases = normalizeAliases(request.getAliases(), documentType);
        assertSlugsAvailable(documentType, aliases);

        LocalDateTime now = LocalDateTime.now();
        LegalDocument document = LegalDocument.builder()
                .documentType(documentType)
                .title(request.getTitle())
                .locale(request.getLocale())
                .version(resolveVersion(request.getVersion()))
                .contentHtml(LegalHtmlSanitizer.sanitize(request.getContentHtml()))
                .aliases(aliases)
                .protectedDocument(PROTECTED_TYPES.contains(documentType))
                .updatedAt(now)
                .build();

        LegalDocument saved = legalDocumentRepository.save(document);
        log.info("Created legal document type '{}' with {} alias(es)", documentType, aliases.size());

        return toResponse(saved);
    }

    @Transactional
    public LegalDocumentResponse createOrUpdateLegalDocument(String documentType, LegalDocumentRequest request) {
        String normalizedType = normalize(documentType);

        Optional<LegalDocument> existing = resolve(normalizedType);
        if (existing.isEmpty() && !PROTECTED_TYPES.contains(normalizedType)) {
            // Custom types must be registered through POST /legal/ first, so a typo in the path
            // cannot silently create a stray document.
            throw notFound(documentType);
        }

        LocalDateTime now = LocalDateTime.now();
        LegalDocument document = existing.orElseGet(() -> LegalDocument.builder()
                .documentType(normalizedType)
                .aliases(defaultAliasesFor(normalizedType))
                .protectedDocument(true)
                .build());

        document.setTitle(request.getTitle());
        document.setLocale(request.getLocale());
        document.setVersion(resolveVersion(request.getVersion()));
        document.setContentHtml(LegalHtmlSanitizer.sanitize(request.getContentHtml()));
        document.setUpdatedAt(now);

        LegalDocument saved = legalDocumentRepository.save(document);
        log.info("Updated legal document type '{}'", saved.getDocumentType());

        return toResponse(saved);
    }

    @Transactional
    public LegalDocumentDeleteResponse deleteLegalDocument(String documentType) {
        LegalDocument document = requireDocument(documentType);

        if (document.isProtectedDocument() || PROTECTED_TYPES.contains(document.getDocumentType())) {
            throw new ApiException(
                    "The " + document.getDocumentType() + " document type cannot be deleted.",
                    HttpStatus.FORBIDDEN,
                    "LEGAL_DOCUMENT_PROTECTED");
        }

        legalDocumentRepository.delete(document);
        log.info("Deleted legal document type '{}'", document.getDocumentType());

        return LegalDocumentDeleteResponse.builder()
                .documentType(document.getDocumentType())
                .deleted(true)
                .build();
    }

    // --- internals ---

    private LegalDocument requireDocument(String documentType) {
        return resolve(normalize(documentType)).orElseThrow(() -> notFound(documentType));
    }

    /** Resolves a slug against canonical document types first, then registered aliases. */
    private Optional<LegalDocument> resolve(String slug) {
        Optional<LegalDocument> byType = legalDocumentRepository.findByDocumentType(slug);
        if (byType.isPresent()) {
            return byType;
        }

        // The table holds a handful of rows, so scanning aliases in memory is cheaper than a
        // native jsonb containment query and keeps the repository portable.
        return legalDocumentRepository.findAll().stream()
                .filter(doc -> doc.getAliases() != null && doc.getAliases().contains(slug))
                .findFirst();
    }

    private void validateSlug(String slug) {
        if (slug.length() < SLUG_MIN_LENGTH || slug.length() > SLUG_MAX_LENGTH || !SLUG_PATTERN.matcher(slug).matches()) {
            throw new BadRequestException(
                    "documentType must be lowercase kebab-case, 3-64 characters: " + slug,
                    "VALIDATION_ERROR");
        }
    }

    /** Rejects a create whose slug or any alias collides with an existing type or alias. */
    private void assertSlugsAvailable(String documentType, List<String> aliases) {
        Set<String> incoming = new LinkedHashSet<>();
        incoming.add(documentType);
        incoming.addAll(aliases);

        for (LegalDocument existing : legalDocumentRepository.findAll()) {
            Set<String> taken = new LinkedHashSet<>();
            taken.add(existing.getDocumentType());
            if (existing.getAliases() != null) {
                taken.addAll(existing.getAliases());
            }

            for (String slug : incoming) {
                if (taken.contains(slug)) {
                    throw new ConflictException(
                            "The slug '" + slug + "' is already used by document type '"
                                    + existing.getDocumentType() + "'.",
                            "LEGAL_DOCUMENT_TYPE_EXISTS");
                }
            }
        }
    }

    private List<String> normalizeAliases(List<String> aliases, String documentType) {
        if (aliases == null || aliases.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String alias : aliases) {
            if (alias == null || alias.isBlank()) {
                continue;
            }
            String slug = normalize(alias);
            validateSlug(slug);
            if (slug.equals(documentType)) {
                throw new BadRequestException(
                        "Alias '" + slug + "' duplicates the documentType", "VALIDATION_ERROR");
            }
            normalized.add(slug);
        }
        return new ArrayList<>(normalized);
    }

    /** Preserves the historical aliases for the two seeded documents. */
    private List<String> defaultAliasesFor(String documentType) {
        return switch (documentType) {
            case "privacy" -> new ArrayList<>(List.of("privacy-policy"));
            case "terms" -> new ArrayList<>(List.of("terms-of-service"));
            default -> new ArrayList<>();
        };
    }

    private String resolveVersion(String version) {
        return version == null || version.isBlank()
                ? LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                : version.trim();
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase().trim();
    }

    private ApiException notFound(String documentType) {
        return new ApiException(
                "Legal document not found for type: " + documentType,
                HttpStatus.NOT_FOUND,
                "LEGAL_DOCUMENT_NOT_FOUND");
    }

    private LegalDocumentResponse toResponse(LegalDocument doc) {
        return LegalDocumentResponse.builder()
                .documentType(doc.getDocumentType())
                .title(doc.getTitle())
                .locale(doc.getLocale())
                .version(doc.getVersion())
                .updatedAt(doc.getUpdatedAt())
                .contentHtml(doc.getContentHtml())
                .aliases(doc.getAliases() == null ? List.of() : doc.getAliases())
                .build();
    }
}
