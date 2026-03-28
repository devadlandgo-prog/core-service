package com.landgo.coreservice.repository;
import com.landgo.coreservice.entity.ListingDraft;
import com.landgo.coreservice.enums.DraftStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface ListingDraftRepository extends JpaRepository<ListingDraft, UUID> {
    @Query("SELECT d FROM ListingDraft d WHERE d.id = :id AND d.owner.id = :ownerId AND d.deleted = false")
    Optional<ListingDraft> findByIdAndOwnerId(@Param("id") UUID id, @Param("ownerId") UUID ownerId);
    @Query("SELECT d FROM ListingDraft d WHERE d.owner.id = :ownerId AND d.status != 'PUBLISHED' AND d.deleted = false ORDER BY d.updatedAt DESC")
    Page<ListingDraft> findActiveByOwnerId(@Param("ownerId") UUID ownerId, Pageable pageable);
    long countByOwnerIdAndStatusAndDeletedFalse(UUID ownerId, DraftStatus status);
}
