package com.landgo.coreservice.service;

import com.landgo.coreservice.dto.request.LandCreateRequest;
import com.landgo.coreservice.dto.response.LandResponse;
import com.landgo.coreservice.dto.response.PageResponse;
import com.landgo.coreservice.dto.response.VendorResponse;
import com.landgo.coreservice.entity.Land;
import com.landgo.coreservice.entity.FavoriteListing;
import com.landgo.coreservice.entity.User;
import com.landgo.coreservice.enums.LandStatus;
import com.landgo.coreservice.enums.ProjectStage;
import com.landgo.coreservice.exception.BadRequestException;
import com.landgo.coreservice.exception.ForbiddenException;
import com.landgo.coreservice.exception.ResourceNotFoundException;
import com.landgo.coreservice.mapper.LandMapper;
import com.landgo.coreservice.repository.FavoriteListingRepository;
import com.landgo.coreservice.repository.LandRepository;
import com.landgo.coreservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LandService {

    private final LandRepository landRepository;
    private final FavoriteListingRepository favoriteRepository;
    private final UserRepository userRepository;
    private final com.landgo.coreservice.repository.VendorRepository vendorRepository;
    private final UserServiceClient userServiceClient;
    private final LandMapper landMapper;

    @Transactional
    public LandResponse createLand(UUID userId, LandCreateRequest request) {
        log.info("Transaction BEGIN: Creating land listing for user: {}", userId);
        
        // Get vendor profile from local database using userId
        com.landgo.coreservice.entity.VendorProfile vendorProfile = vendorRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Transaction ROLLBACK: User {} is not a vendor", userId);
                    return new BadRequestException("You must register as a vendor/seller first");
                });

        Land land = landMapper.toEntity(request);
        land.setVendorId(vendorProfile.getId());
        land.setStatus(LandStatus.PENDING_APPROVAL);

        Land saved = landRepository.save(land);

        log.info("Transaction COMMIT: Land listing {} created by vendor: {}", saved.getId(), vendorProfile.getId());
        return landMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LandResponse getLandById(UUID id) {
        Land land = landRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", id));
        return landMapper.toResponse(land);
    }

    @Transactional
    public LandResponse getLandByIdWithView(UUID id) {
        Land land = landRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", id));
        landRepository.incrementViewCount(id);
        land.setViewCount(land.getViewCount() + 1);
        return landMapper.toResponse(land);
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> getActiveListings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Land> lands = landRepository.findByStatusAndDeletedFalse(LandStatus.ACTIVE, pageable);
        return buildPageResponse(lands);
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> getRecommendations(int page, int size) {
        // Sort by asking price descending (highest price first)
        Pageable pageable = PageRequest.of(page, size, Sort.by("askingPrice").descending());
        Page<Land> lands = landRepository.findByStatusAndDeletedFalse(LandStatus.ACTIVE, pageable);
        return buildPageResponse(lands);
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> getHotDeveloperDeals(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Land> lands = landRepository.findHotDeveloperDeals(pageable);
        return buildPageResponse(lands);
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> getFeaturedListings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Land> lands = landRepository.findFeaturedListings(pageable);
        return buildPageResponse(lands);
    }

    @Transactional
    public Long incrementViewCount(UUID id) {
        Land land = landRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", id));
        landRepository.incrementViewCount(id);
        return land.getViewCount() + 1L;
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> searchLands(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Land> lands = landRepository.searchLands(search, pageable);
        return buildPageResponse(lands);
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> filterLands(String city, ProjectStage stage, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Land> lands = landRepository.findByFilters(city, stage, minPrice, maxPrice, pageable);
        return buildPageResponse(lands);
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> getVendorLands(UUID vendorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Land> lands = landRepository.findByVendorId(vendorId, pageable);
        return buildPageResponse(lands);
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> getMyListings(UUID userId, int page, int size) {
        VendorResponse vendor = userServiceClient.getVendorProfileForUser(userId);
        if (vendor == null) {
            return PageResponse.empty(page, size);
        }
        return getVendorLands(vendor.getId(), page, size);
    }

    @Transactional
    public LandResponse updateLand(UUID userId, UUID landId, LandCreateRequest request) {
        log.info("Transaction BEGIN: Updating land listing {}", landId);
        Land land = landRepository.findByIdAndDeletedFalse(landId)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", landId));

        // Get vendor profile from local database using userId
        com.landgo.coreservice.entity.VendorProfile vendorProfile = vendorRepository.findByUserId(userId)
                .orElseThrow(() -> new ForbiddenException("You are not authorized to update this listing"));
        
        // Check if the land belongs to this vendor (compare vendor profile IDs)
        if (!land.getVendorId().equals(vendorProfile.getId())) {
            log.warn("Transaction ROLLBACK: Unauthorized update attempt for listing {} by user {}", landId, userId);
            throw new ForbiddenException("You are not authorized to update this listing");
        }

        try {
            landMapper.updateEntity(request, land);
            Land updated = landRepository.save(land);
            log.info("Transaction COMMIT: Land listing {} updated", landId);
            return landMapper.toResponse(updated);
        } catch (Exception e) {
            log.error("Error updating land listing {}: {}", landId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void deleteLand(UUID userId, UUID landId) {
        log.info("Attempting to delete listing: {} by user: {}", landId, userId);
        Land land = landRepository.findByIdAndDeletedFalse(landId)
                .orElseThrow(() -> {
                    log.warn("Delete failed: Listing {} not found", landId);
                    return new ResourceNotFoundException("Land", "id", landId);
                });

        // Get vendor profile from local database using userId
        com.landgo.coreservice.entity.VendorProfile vendorProfile = vendorRepository.findByUserId(userId)
                .orElseThrow(() -> new ForbiddenException("You are not authorized to delete this listing"));
        
        // Check if the land belongs to this vendor (compare vendor profile IDs)
        if (!land.getVendorId().equals(vendorProfile.getId())) {
            log.warn("Delete denied: User {} is not the owner of listing {}", userId, landId);
            throw new ForbiddenException("You are not authorized to delete this listing");
        }

        land.setDeleted(true);
        landRepository.save(land);
        log.info("Land listing deleted: {} by user: {}", landId, userId);
    }

    @Transactional
    public LandResponse updateLandStatus(UUID landId, LandStatus status) {
        Land land = landRepository.findByIdAndDeletedFalse(landId)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", landId));
        land.setStatus(status);
        Land updated = landRepository.save(land);
        return landMapper.toResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<LandResponse> getRecentListings(int limit) {
        return landRepository.findRecentListings(PageRequest.of(0, limit))
                .stream().map(landMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LandResponse> getPopularListings(int limit) {
        return landRepository.findPopularListings(PageRequest.of(0, limit))
                .stream().map(landMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> getFavoriteListings(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<FavoriteListing> favorites = favoriteRepository.findByUserId(userId, pageable);
        
        List<LandResponse> content = favorites.getContent().stream()
                .map(f -> landMapper.toResponse(f.getLand()))
                .collect(Collectors.toList());

        return PageResponse.<LandResponse>builder()
                .content(content).number(favorites.getNumber()).size(favorites.getSize())
                .totalElements(favorites.getTotalElements()).totalPages(favorites.getTotalPages())
                .first(favorites.isFirst()).last(favorites.isLast()).build();
    }

    @Transactional
    public void updateProfessionalVerificationStatus(UUID id, com.landgo.coreservice.enums.VerificationStatus status, String notes) {
        com.landgo.coreservice.entity.VendorProfile vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "id", id));
        vendor.setVerificationStatus(status);
        vendor.setVerified(status == com.landgo.coreservice.enums.VerificationStatus.APPROVED);
        vendor.setVerificationNotes(notes);
        vendorRepository.save(vendor);
    }

    @Transactional
    public boolean toggleFavorite(UUID userId, UUID landId) {
        Land land = landRepository.findByIdAndDeletedFalse(landId)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", landId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return favoriteRepository.findByUserIdAndLandId(userId, landId)
                .map(f -> {
                    favoriteRepository.delete(f);
                    return false;
                })
                .orElseGet(() -> {
                    favoriteRepository.save(FavoriteListing.builder()
                            .user(user)
                            .land(land)
                            .build());
                    return true;
                });
    }

    private PageResponse<LandResponse> buildPageResponse(Page<Land> page) {
        List<LandResponse> content = page.getContent().stream().map(landMapper::toResponse).collect(Collectors.toList());
        return PageResponse.<LandResponse>builder()
                .content(content).number(page.getNumber()).size(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .first(page.isFirst()).last(page.isLast()).build();
    }
}
