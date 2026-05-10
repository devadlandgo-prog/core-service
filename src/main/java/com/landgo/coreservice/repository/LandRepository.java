package com.landgo.coreservice.repository;

import com.landgo.coreservice.entity.Land;
import com.landgo.coreservice.enums.LandStatus;
import com.landgo.coreservice.enums.ProjectStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LandRepository extends JpaRepository<Land, UUID>, JpaSpecificationExecutor<Land> {

    Optional<Land> findByIdAndDeletedFalse(UUID id);

    Page<Land> findByStatusAndDeletedFalse(LandStatus status, Pageable pageable);

    // Admin: all listings regardless of status
    Page<Land> findByDeletedFalse(Pageable pageable);

    @Query("SELECT l FROM Land l WHERE l.vendorId = :vendorId AND l.deleted = false")
    Page<Land> findByVendorId(@Param("vendorId") UUID vendorId, Pageable pageable);

    // Using Specification in LandService instead of findByFilters

    @Query("SELECT l FROM Land l WHERE l.status = 'ACTIVE' AND l.deleted = false ORDER BY l.createdAt DESC")
    List<Land> findRecentListings(Pageable pageable);

    @Query("SELECT l FROM Land l WHERE l.status = 'ACTIVE' AND l.deleted = false ORDER BY l.viewCount DESC")
    List<Land> findPopularListings(Pageable pageable);

    @Query("SELECT l FROM Land l WHERE l.status = 'ACTIVE' AND l.deleted = false AND l.isFeatured = true")
    Page<Land> findFeaturedListings(Pageable pageable);

    @Query("SELECT l FROM Land l WHERE l.status = 'ACTIVE' AND l.deleted = false AND l.isHotDeal = true")
    Page<Land> findHotDeveloperDeals(Pageable pageable);

    @Modifying
    @Query("UPDATE Land l SET l.viewCount = l.viewCount + 1 WHERE l.id = :id")
    void incrementViewCount(@Param("id") UUID id);

    @Query("SELECT COUNT(l) FROM Land l WHERE l.vendorId = :vendorId AND l.status = 'ACTIVE' AND l.deleted = false")
    long countActiveListingsByVendorId(@Param("vendorId") UUID vendorId);

    @Query("SELECT COALESCE(SUM(l.viewCount), 0) FROM Land l WHERE l.vendorId = :vendorId AND l.deleted = false")
    long sumViewCountByVendorId(@Param("vendorId") UUID vendorId);

    @Query("SELECT COUNT(l) FROM Land l WHERE l.vendorId = :vendorId AND l.deleted = false")
    long countAllByVendorIdAndDeletedFalse(@Param("vendorId") UUID vendorId);

    long countByStatus(LandStatus status);
}