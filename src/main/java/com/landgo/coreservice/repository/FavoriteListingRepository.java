package com.landgo.coreservice.repository;

import com.landgo.coreservice.entity.FavoriteListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriteListingRepository extends JpaRepository<FavoriteListing, UUID> {

    @Query("SELECT f FROM FavoriteListing f WHERE f.userId = :userId AND f.landId = :landId")
    Optional<FavoriteListing> findByUserIdAndLandId(@Param("userId") UUID userId, @Param("landId") UUID landId);

    @Query("SELECT f FROM FavoriteListing f WHERE f.userId = :userId")
    Page<FavoriteListing> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Transactional
    @Modifying
    @Query("DELETE FROM FavoriteListing f WHERE f.userId = :userId AND f.landId = :landId")
    void deleteByUserIdAndLandId(@Param("userId") UUID userId, @Param("landId") UUID landId);
}