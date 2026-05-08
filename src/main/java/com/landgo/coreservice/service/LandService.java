package com.landgo.coreservice.service;

import com.landgo.coreservice.dto.request.LandCreateRequest;
import com.landgo.coreservice.dto.response.LandResponse;
import com.landgo.coreservice.dto.response.VendorResponse;
import com.landgo.coreservice.dto.response.PageResponse;
import com.landgo.coreservice.entity.FavoriteListing;
import com.landgo.coreservice.entity.Land;
import com.landgo.coreservice.enums.LandStatus;
import com.landgo.coreservice.enums.ProjectStage;
import com.landgo.coreservice.exception.ForbiddenException;
import com.landgo.coreservice.exception.ResourceNotFoundException;
import com.landgo.coreservice.mapper.LandMapper;
import com.landgo.coreservice.repository.FavoriteListingRepository;
import com.landgo.coreservice.repository.LandRepository;
import com.landgo.coreservice.repository.LandSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    private final UserServiceClient userServiceClient;
    private final LandMapper landMapper;

    @Transactional
    public LandResponse createLand(LandCreateRequest request, UUID vendorId) {
        log.debug("Creating land for vendorId: {}", vendorId);
        Land land = landMapper.toEntity(request);
        land.setVendorId(vendorId);
        land.setStatus(LandStatus.PENDING_APPROVAL);
        land.setFeatured(false);
        land.setViewCount(0);
        land.setInquiryCount(0);
        Land saved = landRepository.save(land);
        log.debug("Land created with id: {}", saved.getId());
        return getLandResponseWithFavorite(saved, vendorId);
    }

    @Transactional(readOnly = true)
    public LandResponse getLandById(UUID id, UUID currentUserId) {
        Land land = landRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", id));
        return getLandResponseWithFavorite(land, currentUserId);
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> getActiveLands(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<Land> lands = landRepository.findByStatusAndDeletedFalse(LandStatus.ACTIVE, pageable);
        return getPageResponse(lands);
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> searchLands(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Land> spec = LandSpecification.searchLands(query)
                .and(LandSpecification.hasStatus("ACTIVE"))
                .and(LandSpecification.isNotDeleted());
        Page<Land> lands = landRepository.findAll(spec, pageable);
        return getPageResponse(lands);
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> filterLands(String city, ProjectStage stage, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Land> lands = landRepository.findByFilters(city, stage, minPrice, maxPrice, pageable);
        return getPageResponse(lands);
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> getMyLands(UUID vendorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Land> lands = landRepository.findByVendorId(vendorId, pageable);
        return getPageResponse(lands);
    }

    @Transactional(readOnly = true)
    public List<LandResponse> getRecentLands(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Land> lands = landRepository.findRecentListings(pageable);
        return lands.stream().map(land -> getLandResponseWithFavorite(land, null)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LandResponse> getPopularLands(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "viewCount"));
        List<Land> lands = landRepository.findPopularListings(pageable);
        return lands.stream().map(land -> getLandResponseWithFavorite(land, null)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LandResponse> getFeaturedLands(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Land> lands = landRepository.findFeaturedListings(pageable).getContent();
        return lands.stream().map(land -> getLandResponseWithFavorite(land, null)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LandResponse> getHotDeveloperDeals(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Land> lands = landRepository.findHotDeveloperDeals(pageable).getContent();
        return lands.stream().map(land -> getLandResponseWithFavorite(land, null)).collect(Collectors.toList());
    }

    @Transactional
    public LandResponse updateLandStatus(UUID id, LandStatus status) {
        Land land = landRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", id));
        land.setStatus(status);
        Land saved = landRepository.save(land);
        return getLandResponseWithFavorite(saved, null);
    }

    @Transactional
    public LandResponse updateLand(UUID id, LandCreateRequest request, UUID userId) {
        Land land = landRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", id));
        if (!land.getVendorId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to update this listing");
        }
        landMapper.updateEntity(request, land);
        Land saved = landRepository.save(land);
        return getLandResponseWithFavorite(saved, userId);
    }

    @Transactional
    public void deleteLand(UUID id, UUID userId) {
        Land land = landRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", id));
        if (!land.getVendorId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to delete this listing");
        }
        land.setDeleted(true);
        landRepository.save(land);
        log.info("Land {} deleted by user {}", id, userId);
    }

    @Transactional
    public boolean toggleFavorite(UUID userId, UUID landId) {
        Land land = landRepository.findByIdAndDeletedFalse(landId)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", landId));
        
        return favoriteRepository.findByUserIdAndLandId(userId, landId)
                .map(f -> {
                    favoriteRepository.delete(f);
                    return false;
                })
                .orElseGet(() -> {
                    favoriteRepository.save(FavoriteListing.builder()
                            .userId(userId)
                            .landId(land.getId())
                            .build());
                    return true;
                });
    }

    @Transactional(readOnly = true)
    public List<LandResponse> getFavoriteLands(UUID userId) {
        List<FavoriteListing> favorites = favoriteRepository.findByUserId(userId, Pageable.unpaged()).getContent();
        return favorites.stream()
                .map(f -> {
                    Land land = landRepository.findByIdAndDeletedFalse(f.getLandId())
                            .orElseThrow(() -> new ResourceNotFoundException("Land", "id", f.getLandId()));
                    return getLandResponseWithFavorite(land, userId);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void incrementViewCount(UUID id) {
        landRepository.incrementViewCount(id);
    }

    @Transactional(readOnly = true)
    public Object getVendorProfile(UUID vendorId) {
        return userServiceClient.getVendorProfileForUser(vendorId);
    }

    @Transactional(readOnly = true)
    public List<LandResponse> getLandsByVendor(UUID vendorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Land> lands = landRepository.findByVendorId(vendorId, pageable).getContent();
        return lands.stream().map(land -> getLandResponseWithFavorite(land, null)).collect(Collectors.toList());
    }

    private LandResponse getLandResponseWithFavorite(Land land, UUID currentUserId) {
        boolean isFavorited = currentUserId != null && 
                favoriteRepository.findByUserIdAndLandId(currentUserId, land.getId()).isPresent();
        
        LandResponse response = landMapper.toResponse(land);
        response.setFavorited(isFavorited);
        
        // Fetch vendor details from user-service
        VendorResponse vendor = userServiceClient.getVendorProfileForUser(land.getVendorId());
        if (vendor != null) {
            response.setVendorCompanyName(vendor.getCompanyName());
            response.setVendorVerified(vendor.isVerified());
            response.setVendorOwnerName(vendor.getOwnerName());
            response.setVendorOwnerEmail(vendor.getOwnerEmail());
        }
        
        return response;
    }

    private PageResponse<LandResponse> getPageResponse(Page<Land> lands) {
        List<LandResponse> content = lands.getContent().stream()
                .map(land -> getLandResponseWithFavorite(land, null))
                .collect(Collectors.toList());
        return PageResponse.<LandResponse>builder()
                .content(content).number(lands.getNumber()).size(lands.getSize())
                .totalElements(lands.getTotalElements()).totalPages(lands.getTotalPages())
                .first(lands.isFirst()).last(lands.isLast()).build();
    }
}