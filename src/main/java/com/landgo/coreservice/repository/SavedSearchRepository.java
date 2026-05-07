package com.landgo.coreservice.repository;
import com.landgo.coreservice.entity.SavedSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface SavedSearchRepository extends JpaRepository<SavedSearch, UUID> {
    Page<SavedSearch> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    List<SavedSearch> findByUserIdAndDeletedFalse(UUID userId);
    Optional<SavedSearch> findByIdAndUserIdAndDeletedFalse(UUID id, UUID userId);
    long countByUserIdAndDeletedFalse(UUID userId);
    @Query("SELECT s FROM SavedSearch s WHERE s.notificationsEnabled = true AND s.deleted = false")
    List<SavedSearch> findAllWithNotificationsEnabled();
    boolean existsByUserIdAndNameAndDeletedFalse(UUID userId, String name);
}
