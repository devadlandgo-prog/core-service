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
import java.util.Map;
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
    public PageResponse<LandResponse> getActiveLands(int page, int size, String sortBy, String sortDir, UUID userId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<Land> lands = landRepository.findByStatusAndDeletedFalse(LandStatus.ACTIVE, pageable);
        return getPageResponse(lands, userId);
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> getAllListingsForAdmin(LandStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Land> lands = (status != null)
                ? landRepository.findByStatusAndDeletedFalse(status, pageable)
                : landRepository.findByDeletedFalse(pageable);
        return getPageResponse(lands, null);
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> searchLands(String query, int page, int size, UUID userId) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Land> spec = LandSpecification.searchLands(query)
                .and(LandSpecification.hasStatus("ACTIVE"))
                .and(LandSpecification.isNotDeleted());
        Page<Land> lands = landRepository.findAll(spec, pageable);
        return getPageResponse(lands, userId);
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> filterLands(String city, ProjectStage stage, BigDecimal minPrice, BigDecimal maxPrice, 
                                               BigDecimal minLotSize, BigDecimal maxLotSize, Boolean isFeatured, Boolean isHotDeal,
                                               String projectType, String buildingType, String zoningType, String listingType, Integer forSaleSince,
                                               String sortBy,
                                               int page, int size, UUID userId) {
        
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (sortBy != null) {
            switch (sortBy.toLowerCase()) {
                case "rating" -> sort = Sort.by(Sort.Direction.DESC, "vendor.rating");
                case "reviews", "most_reviews" -> sort = Sort.by(Sort.Direction.DESC, "vendor.totalReviews");
                case "experience", "most_experience" -> sort = Sort.by(Sort.Direction.DESC, "vendor.yearsOfExperience");
                case "price_asc" -> sort = Sort.by(Sort.Direction.ASC, "askingPrice");
                case "price_desc" -> sort = Sort.by(Sort.Direction.DESC, "askingPrice");
                case "newest" -> sort = Sort.by(Sort.Direction.DESC, "createdAt");
            }
        }
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Specification<Land> spec = Specification.where(LandSpecification.hasStatus("ACTIVE"))
                .and(LandSpecification.isNotDeleted());

        if (city != null && !city.isBlank()) spec = spec.and(LandSpecification.hasCity(city));
        if (stage != null) spec = spec.and(LandSpecification.hasProjectStage(stage));
        if (minPrice != null) spec = spec.and(LandSpecification.hasMinPrice(minPrice));
        if (maxPrice != null) spec = spec.and(LandSpecification.hasMaxPrice(maxPrice));
        if (minLotSize != null) spec = spec.and(LandSpecification.hasMinLotSize(minLotSize));
        if (maxLotSize != null) spec = spec.and(LandSpecification.hasMaxLotSize(maxLotSize));
        if (isFeatured != null) spec = spec.and(LandSpecification.isFeatured(isFeatured));
        if (isHotDeal != null) spec = spec.and(LandSpecification.isHotDeal(isHotDeal));
        
        // New filters
        if (projectType != null && !projectType.isBlank()) spec = spec.and(LandSpecification.hasProjectType(projectType));
        if (buildingType != null && !buildingType.isBlank()) spec = spec.and(LandSpecification.hasBuildingType(buildingType));
        if (listingType != null && !listingType.isBlank()) spec = spec.and(LandSpecification.hasListingType(listingType));
        if (zoningType != null && !zoningType.isBlank()) spec = spec.and(LandSpecification.hasZoningType(zoningType));
        if (forSaleSince != null) spec = spec.and(LandSpecification.forSaleSince(forSaleSince));

        Page<Land> lands = landRepository.findAll(spec, pageable);
        return getPageResponse(lands, userId);
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> getMyLands(UUID vendorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Land> lands = landRepository.findByVendorId(vendorId, pageable);
        return getPageResponse(lands, vendorId);
    }

    @Transactional(readOnly = true)
    public List<LandResponse> getRecentLands(int limit, UUID userId) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Land> lands = landRepository.findByStatusAndDeletedFalse(LandStatus.ACTIVE, pageable).getContent();
        List<LandResponse> responses = lands.stream().map(land -> getLandResponseWithFavorite(land, userId)).collect(Collectors.toList());
        return enrichWithVendors(responses);
    }

    @Transactional(readOnly = true)
    public List<LandResponse> getPopularLands(int limit, UUID userId) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "viewCount"));
        List<Land> lands = landRepository.findByStatusAndDeletedFalse(LandStatus.ACTIVE, pageable).getContent();
        List<LandResponse> responses = lands.stream().map(land -> getLandResponseWithFavorite(land, userId)).collect(Collectors.toList());
        return enrichWithVendors(responses);
    }

    @Transactional(readOnly = true)
    public List<LandResponse> getFeaturedLands(int limit, UUID userId) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Land> lands = landRepository.findFeaturedListings(pageable).getContent();
        List<LandResponse> responses = lands.stream().map(land -> getLandResponseWithFavorite(land, userId)).collect(Collectors.toList());
        return enrichWithVendors(responses);
    }

    @Transactional(readOnly = true)
    public List<LandResponse> getHotDeveloperDeals(int limit, UUID userId) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Land> lands = landRepository.findHotDeveloperDeals(pageable).getContent();
        List<LandResponse> responses = lands.stream().map(land -> getLandResponseWithFavorite(land, userId)).collect(Collectors.toList());
        return enrichWithVendors(responses);
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
    public LandResponse updateLand(UUID id, LandCreateRequest request, UUID userId, boolean isAdmin) {
        Land land = landRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", id));
        if (!isAdmin && !land.getVendorId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to update this listing");
        }
        landMapper.updateEntity(request, land);
        Land saved = landRepository.save(land);
        return getLandResponseWithFavorite(saved, userId);
    }

    @Transactional
    public void deleteLand(UUID id, UUID userId, boolean isAdmin) {
        Land land = landRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", id));
        if (!isAdmin && !land.getVendorId().equals(userId)) {
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
        
        if (favorites.isEmpty()) {
            throw new ResourceNotFoundException("No favorites found for this user");
        }

        List<LandResponse> result = favorites.stream()
                .map(f -> landRepository.findByIdAndDeletedFalse(f.getLandId()))
                .filter(java.util.Optional::isPresent)
                .map(opt -> {
                    Land land = opt.get();
                    boolean isFavorited = favoriteRepository.findByUserIdAndLandId(userId, land.getId()).isPresent();
                    LandResponse response = landMapper.toResponse(land);
                    response.setFavorited(isFavorited);
                    return response;
                })
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            throw new ResourceNotFoundException("All favorited properties have been removed or are no longer active");
        }

        return enrichWithVendors(result);
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
        Page<Land> lands = landRepository.findByVendorId(vendorId, pageable);
        List<LandResponse> responses = lands.getContent().stream().map(land -> getLandResponseWithFavorite(land, null)).collect(Collectors.toList());
        return enrichWithVendors(responses);
    }

    @Transactional
    public LandResponse addImageMetadata(UUID id, UUID vendorId, com.landgo.coreservice.dto.request.ImageConfirmRequest request) {
        Land land = landRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", id));
        if (!land.getVendorId().equals(vendorId)) {
            throw new ForbiddenException("You are not authorized to modify this listing");
        }

        List<java.util.Map<String, String>> photos = land.getPhotos();
        if (photos == null) {
            photos = new java.util.ArrayList<>();
        }

        if (request.isPrimary()) {
            for (java.util.Map<String, String> photo : photos) {
                photo.put("isPrimary", "false");
            }
        }

        java.util.Map<String, String> newPhoto = new java.util.HashMap<>();
        newPhoto.put("url", "https://" + System.getenv("AWS_S3_IMAGES_BUCKET") + ".s3." + System.getenv("AWS_REGION") + ".amazonaws.com/" + request.getFileKey());
        newPhoto.put("fileKey", request.getFileKey());
        newPhoto.put("fileName", request.getFileName());
        newPhoto.put("isPrimary", String.valueOf(request.isPrimary()));
        newPhoto.put("uploadedAt", java.time.LocalDateTime.now().toString());

        photos.add(newPhoto);
        land.setPhotos(photos);
        landRepository.save(land);

        return getLandResponseWithFavorite(land, vendorId);
    }

    @Transactional
    public LandResponse removeImageMetadata(UUID id, UUID vendorId, String fileKey) {
        Land land = landRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", id));
        if (!land.getVendorId().equals(vendorId)) {
            throw new ForbiddenException("You are not authorized to modify this listing");
        }

        List<java.util.Map<String, String>> photos = land.getPhotos();
        if (photos != null) {
            photos.removeIf(p -> fileKey.equals(p.get("fileKey")));
            land.setPhotos(photos);
            landRepository.save(land);
        }

        return getLandResponseWithFavorite(land, vendorId);
    }

    @Transactional
    public LandResponse addDocumentMetadata(UUID id, UUID vendorId, com.landgo.coreservice.dto.request.ImageConfirmRequest request) {
        Land land = landRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", id));
        if (!land.getVendorId().equals(vendorId)) {
            throw new ForbiddenException("You are not authorized to modify this listing");
        }

        List<java.util.Map<String, String>> documents = land.getDocuments();
        if (documents == null) {
            documents = new java.util.ArrayList<>();
        }

        java.util.Map<String, String> newDoc = new java.util.HashMap<>();
        newDoc.put("url", "https://" + System.getenv("AWS_S3_IMAGES_BUCKET") + ".s3." + System.getenv("AWS_REGION") + ".amazonaws.com/" + request.getFileKey());
        newDoc.put("fileKey", request.getFileKey());
        newDoc.put("fileName", request.getFileName());
        newDoc.put("uploadedAt", java.time.LocalDateTime.now().toString());

        documents.add(newDoc);
        land.setDocuments(documents);
        landRepository.save(land);

        return getLandResponseWithFavorite(land, vendorId);
    }

    @Transactional
    public LandResponse removeDocumentMetadata(UUID id, UUID vendorId, String fileKey) {
        Land land = landRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", id));
        if (!land.getVendorId().equals(vendorId)) {
            throw new ForbiddenException("You are not authorized to modify this listing");
        }

        List<java.util.Map<String, String>> documents = land.getDocuments();
        if (documents != null) {
            documents.removeIf(p -> fileKey.equals(p.get("fileKey")));
            land.setDocuments(documents);
            landRepository.save(land);
        }

        return getLandResponseWithFavorite(land, vendorId);
    }

    private List<LandResponse> enrichWithVendors(List<LandResponse> responses) {
        if (responses == null || responses.isEmpty()) return responses;
        
        List<UUID> vendorIds = responses.stream()
                .map(LandResponse::getVendorId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<UUID, VendorResponse> vendorMap = userServiceClient.getVendorProfilesBatch(vendorIds);
        
        responses.forEach(response -> {
            VendorResponse vendor = vendorMap.get(response.getVendorId());
            if (vendor != null) {
                response.setVendorCompanyName(vendor.getCompanyName());
                response.setVendorVerified(vendor.isVerified());
                response.setVendorOwnerName(vendor.getOwnerName());
                response.setVendorOwnerEmail(vendor.getOwnerEmail());
            }
        });
        
        return responses;
    }

    private LandResponse getLandResponseWithFavorite(Land land, UUID currentUserId) {
        boolean isFavorited = currentUserId != null && 
                favoriteRepository.findByUserIdAndLandId(currentUserId, land.getId()).isPresent();
        
        LandResponse response = landMapper.toResponse(land);
        response.setFavorited(isFavorited);
        
        VendorResponse vendor = userServiceClient.getVendorProfileForUser(land.getVendorId());
        if (vendor != null) {
            response.setVendorCompanyName(vendor.getCompanyName());
            response.setVendorVerified(vendor.isVerified());
            response.setVendorOwnerName(vendor.getOwnerName());
            response.setVendorOwnerEmail(vendor.getOwnerEmail());
        }
        
        return response;
    }

    private PageResponse<LandResponse> getPageResponse(Page<Land> lands, UUID userId) {
        List<LandResponse> content = lands.getContent().stream()
                .map(land -> {
                    boolean isFavorited = userId != null && 
                            favoriteRepository.findByUserIdAndLandId(userId, land.getId()).isPresent();
                    LandResponse response = landMapper.toResponse(land);
                    response.setFavorited(isFavorited);
                    return response;
                })
                .collect(Collectors.toList());
        
        enrichWithVendors(content);
        
        return PageResponse.<LandResponse>builder()
                .content(content).number(lands.getNumber()).size(lands.getSize())
                .totalElements(lands.getTotalElements()).totalPages(lands.getTotalPages())
                .first(lands.isFirst()).last(lands.isLast()).build();
    }
}