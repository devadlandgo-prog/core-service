package com.landgo.coreservice.repository;

import com.landgo.coreservice.entity.FavoriteListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriteListingRepository extends JpaRepository<FavoriteListing, UUID> {
    
    @Query("SELECT f FROM FavoriteListing f WHERE f.user.id = :userId AND f.land.id = :landId")
    Optional<FavoriteListing> findByUserIdAndLandId(@Param("userId") UUID userId, @Param("landId") UUID landId);

    @Query("SELECT f FROM FavoriteListing f JOIN FETCH f.land WHERE f.user.id = :userId")
    Page<FavoriteListing> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    void deleteByUserIdAndLandId(UUID userId, UUID landId);
}
