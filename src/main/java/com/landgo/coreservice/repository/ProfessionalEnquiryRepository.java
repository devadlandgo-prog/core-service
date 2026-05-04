package com.landgo.coreservice.repository;

import com.landgo.coreservice.entity.ProfessionalEnquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProfessionalEnquiryRepository extends JpaRepository<ProfessionalEnquiry, UUID> {
    List<ProfessionalEnquiry> findByVendorIdOrderByCreatedAtDesc(UUID vendorId);
    long countByVendorId(UUID vendorId);
}
