package com.landgo.coreservice.repository;

import com.landgo.coreservice.entity.Enquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EnquiryRepository extends JpaRepository<Enquiry, UUID> {

    @Query("SELECT COUNT(e) FROM Enquiry e, Land l WHERE e.listingId = l.id AND l.vendorId = :vendorId AND l.deleted = false")
    long countEnquiriesForVendorListings(@Param("vendorId") UUID vendorId);
}
