package com.landgo.coreservice.service;

import com.landgo.coreservice.dto.request.LegalDocumentRequest;
import com.landgo.coreservice.dto.response.LegalDocumentResponse;
import com.landgo.coreservice.entity.LegalDocument;
import com.landgo.coreservice.exception.ResourceNotFoundException;
import com.landgo.coreservice.repository.LegalDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LegalDocumentService {

    private final LegalDocumentRepository legalDocumentRepository;

    @Transactional(readOnly = true)
    public LegalDocumentResponse getLegalDocument(String documentType) {
        String normalizedType = documentType.toLowerCase().trim();
        if ("privacy-policy".equals(normalizedType)) {
            normalizedType = "privacy";
        } else if ("terms-of-service".equals(normalizedType)) {
            normalizedType = "terms";
        }

        if (!"privacy".equals(normalizedType) && !"terms".equals(normalizedType)) {
            throw new ResourceNotFoundException("LegalDocument", "documentType", documentType);
        }

        LegalDocument doc = legalDocumentRepository.findByDocumentType(normalizedType)
                .orElseThrow(() -> new ResourceNotFoundException("LegalDocument", "documentType", documentType));

        return LegalDocumentResponse.builder()
                .documentType(doc.getDocumentType())
                .title(doc.getTitle())
                .locale(doc.getLocale())
                .version(doc.getVersion())
                .updatedAt(doc.getUpdatedAt())
                .contentHtml(doc.getContentHtml())
                .build();
    }

    @Transactional
    public LegalDocumentResponse createOrUpdateLegalDocument(String documentType, LegalDocumentRequest request) {
        String normalizedType = documentType.toLowerCase().trim();
        if ("privacy-policy".equals(normalizedType)) {
            normalizedType = "privacy";
        } else if ("terms-of-service".equals(normalizedType)) {
            normalizedType = "terms";
        }

        if (!"privacy".equals(normalizedType) && !"terms".equals(normalizedType)) {
            throw new com.landgo.coreservice.exception.BadRequestException(
                    "Invalid documentType: " + documentType, "VALIDATION_ERROR");
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        String finalNormalizedType = normalizedType;
        LegalDocument doc = legalDocumentRepository.findByDocumentType(finalNormalizedType)
                .map(existing -> {
                    existing.setTitle(request.getTitle());
                    existing.setLocale(request.getLocale());
                    existing.setVersion(request.getVersion());
                    existing.setContentHtml(request.getContentHtml());
                    existing.setUpdatedAt(now);
                    return existing;
                })
                .orElseGet(() -> LegalDocument.builder()
                        .documentType(finalNormalizedType)
                        .title(request.getTitle())
                        .locale(request.getLocale())
                        .version(request.getVersion())
                        .contentHtml(request.getContentHtml())
                        .updatedAt(now)
                        .build());

        LegalDocument saved = legalDocumentRepository.save(doc);

        return LegalDocumentResponse.builder()
                .documentType(saved.getDocumentType())
                .title(saved.getTitle())
                .locale(saved.getLocale())
                .version(saved.getVersion())
                .updatedAt(saved.getUpdatedAt())
                .contentHtml(saved.getContentHtml())
                .build();
    }
}
