package com.landgo.coreservice.repository;

import com.landgo.coreservice.entity.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FaqRepository extends JpaRepository<Faq, UUID> {

    Optional<Faq> findByIdAndDeletedFalse(UUID id);

    /**
     * Live FAQs in display order: {@code sortOrder} first, then question, exactly as the public
     * page and the admin table both render them.
     */
    @Query("SELECT f FROM Faq f WHERE f.deleted = false ORDER BY f.sortOrder ASC, f.question ASC")
    List<Faq> findAllLive();

    @Query("SELECT f FROM Faq f WHERE f.deleted = false AND LOWER(f.category) = LOWER(:category) "
            + "ORDER BY f.sortOrder ASC, f.question ASC")
    List<Faq> findLiveByCategory(@Param("category") String category);

    @Query("SELECT DISTINCT f.category FROM Faq f WHERE f.deleted = false AND f.category IS NOT NULL "
            + "ORDER BY f.category ASC")
    List<String> findLiveCategories();

    boolean existsByQuestionIgnoreCaseAndDeletedFalse(String question);
}
