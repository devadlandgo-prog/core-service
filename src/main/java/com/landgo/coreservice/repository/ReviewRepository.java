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
    @Query("SELECT r FROM Review r JOIN FETCH r.author WHERE r.professional.id = :professionalId AND r.deleted = false ORDER BY r.createdAt DESC")
    Page<Review> findByProfessionalId(@Param("professionalId") UUID professionalId, Pageable pageable);
    boolean existsByAuthorIdAndProfessionalId(UUID authorId, UUID professionalId);
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.professional.id = :professionalId AND r.deleted = false")
    Double getAverageRatingByProfessionalId(@Param("professionalId") UUID professionalId);
    @Query("SELECT COUNT(r) FROM Review r WHERE r.professional.id = :professionalId AND r.deleted = false")
    long countByProfessionalId(@Param("professionalId") UUID professionalId);
}
