package com.landgo.coreservice.controller;

import com.landgo.coreservice.dto.request.CreateReviewRequest;
import com.landgo.coreservice.dto.response.ApiResponse;
import com.landgo.coreservice.dto.response.PageResponse;
import com.landgo.coreservice.dto.response.ReviewResponse;
import com.landgo.coreservice.security.CurrentUser;
import com.landgo.coreservice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/professionals/{professionalId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @CurrentUser UUID userId,
            @PathVariable UUID professionalId,
            @Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse review = reviewService.createReview(userId, professionalId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted successfully", review));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getReviewsForProfessional(
            @PathVariable UUID professionalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<ReviewResponse> reviews = reviewService.getReviewsForProfessional(professionalId, page, size);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @CurrentUser UUID userId,
            @PathVariable UUID reviewId) {
        reviewService.deleteReview(userId, reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", null));
    }
}
