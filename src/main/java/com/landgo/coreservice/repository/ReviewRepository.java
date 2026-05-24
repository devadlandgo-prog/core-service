package com.landgo.coreservice.repository;

import com.landgo.coreservice.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    
    @Query("SELECT r FROM Review r WHERE r.professionalId = :professionalId AND r.deleted = false ORDER BY r.createdAt DESC")
    Page<Review> findByProfessionalId(@Param("professionalId") UUID professionalId, Pageable pageable);

    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.authorId = :authorId AND r.professionalId = :professionalId AND r.deleted = false")
    boolean existsByAuthorIdAndProfessionalIdAndDeletedFalse(@Param("authorId") UUID authorId, @Param("professionalId") UUID professionalId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.professionalId = :professionalId AND r.deleted = false")
    Double getAverageRatingByProfessionalId(@Param("professionalId") UUID professionalId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.professionalId = :professionalId AND r.deleted = false")
    long countByProfessionalId(@Param("professionalId") UUID professionalId);
}
