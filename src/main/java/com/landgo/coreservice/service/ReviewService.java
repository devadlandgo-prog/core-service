package com.landgo.coreservice.service;

import com.landgo.coreservice.dto.request.CreateReviewRequest;
import com.landgo.coreservice.dto.response.PageResponse;
import com.landgo.coreservice.dto.response.ReviewResponse;
import com.landgo.coreservice.dto.response.UserResponse;
import com.landgo.coreservice.entity.Review;
import com.landgo.coreservice.exception.BadRequestException;
import com.landgo.coreservice.exception.ConflictException;
import com.landgo.coreservice.exception.ForbiddenException;
import com.landgo.coreservice.exception.ResourceNotFoundException;
import com.landgo.coreservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserServiceClient userServiceClient;

    @Transactional
    public ReviewResponse createReview(UUID authorId, UUID professionalId, CreateReviewRequest request) {
        if (reviewRepository.existsByAuthorIdAndProfessionalId(authorId, professionalId)) {
            throw new ConflictException("You have already reviewed this professional", "DUPLICATE_REVIEW");
        }

        // Verify professional exists in user-service
        UserResponse professional = userServiceClient.getUserById(professionalId);
        if (professional == null) {
            throw new ResourceNotFoundException("Professional", "id", professionalId);
        }

        if (professionalId.equals(authorId)) {
            throw new BadRequestException("You cannot review yourself");
        }

        Review review = Review.builder()
                .authorId(authorId)
                .professionalId(professionalId)
                .rating(request.getRating())
                .title(request.getTitle())
                .content(request.getContent())
                .tags(request.getTags())
                .photos(request.getPhotos())
                .verifiedPurchase(false)
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Review created by {} for professional {}", authorId, professionalId);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getReviewsForProfessional(UUID professionalId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews = reviewRepository.findByProfessionalId(professionalId, pageable);
        List<ReviewResponse> content = reviews.getContent().stream().map(this::toResponse).collect(Collectors.toList());
        return PageResponse.<ReviewResponse>builder()
                .content(content).number(reviews.getNumber()).size(reviews.getSize())
                .totalElements(reviews.getTotalElements()).totalPages(reviews.getTotalPages())
                .first(reviews.isFirst()).last(reviews.isLast()).build();
    }

    @Transactional
    public void deleteReview(UUID userId, UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getAuthorId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to delete this review");
        }

        review.setDeleted(true);
        reviewRepository.save(review);
        log.info("Review {} deleted by user {}", reviewId, userId);
    }

    private ReviewResponse toResponse(Review review) {
        UserResponse author = userServiceClient.getUserById(review.getAuthorId());
        return ReviewResponse.builder()
                .id(review.getId())
                .professionalId(review.getProfessionalId())
                .authorId(review.getAuthorId())
                .authorName(author != null ? author.getFullName() : "Unknown User")
                .authorAvatarUrl(author != null ? author.getProfileImageUrl() : null)
                .rating(review.getRating())
                .title(review.getTitle())
                .content(review.getContent())
                .tags(review.getTags())
                .photos(review.getPhotos())
                .verifiedPurchase(review.isVerifiedPurchase())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
