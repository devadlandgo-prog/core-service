package com.landgo.coreservice.repository;

import com.landgo.coreservice.entity.LegalDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LegalDocumentRepository extends JpaRepository<LegalDocument, UUID> {
    Optional<LegalDocument> findByDocumentType(String documentType);
}
