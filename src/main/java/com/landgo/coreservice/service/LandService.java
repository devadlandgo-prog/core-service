package com.landgo.coreservice.service;

import com.landgo.coreservice.dto.request.LandCreateRequest;
import com.landgo.coreservice.dto.response.LandResponse;
import com.landgo.coreservice.dto.response.VendorResponse;
import com.landgo.coreservice.dto.response.UserResponse;
import com.landgo.coreservice.dto.response.PageResponse;
import com.landgo.coreservice.entity.FavoriteListing;
import com.landgo.coreservice.entity.Land;
import com.landgo.coreservice.enums.LandStatus;
import com.landgo.coreservice.enums.ProjectStage;
import com.landgo.coreservice.enums.ProjectType;
import com.landgo.coreservice.enums.BuildingType;
import com.landgo.coreservice.enums.ZoningType;
import com.landgo.coreservice.enums.ListingType;
import com.landgo.coreservice.exception.BadRequestException;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LandService {

    private final LandRepository landRepository;
    private final FavoriteListingRepository favoriteRepository;
    private final UserServiceClient userServiceClient;
    private final LandMapper landMapper;
    private final com.landgo.coreservice.repository.ListingDraftRepository draftRepository;

    @org.springframework.beans.factory.annotation.Value("${app.web.my-listings-url:https://landgo.ca/my-listings}")
    private String myListingsUrl;

    @org.springframework.beans.factory.annotation.Value("${app.web.listing-base-url:https://landgo.ca/listings}")
    private String publicListingBaseUrl;

    @Transactional
    public LandResponse createLand(LandCreateRequest request, UUID vendorId) {
        log.debug("Creating land for vendorId: {}", vendorId);

        // Posting a listing costs one credit. Checked here, before any work, so a user with no
        // credits gets a clear message rather than a listing that fails to publish later.
        UserServiceClient.ListingCredits credits = userServiceClient.getListingCredits(vendorId);
        if (credits != null && credits.available() <= 0) {
            throw new com.landgo.coreservice.exception.BadRequestException(
                    "You have no listing credits left. Buy a land listing package to post another "
                            + "listing — credits never expire.",
                    "NO_LISTING_CREDITS");
        }

        // MLS Validation logic
        if (Boolean.TRUE.equals(request.getMls())) {
            if (request.getMlsMobileNumber() == null || request.getMlsMobileNumber().isBlank()) {
                throw new BadRequestException("mlsMobileNumber is required when mls is true", "VALIDATION_ERROR");
            }
            if (!request.getMlsMobileNumber().matches("^\\+?[0-9\\s\\-\\(\\)]{7,20}$")) {
                throw new BadRequestException("Invalid mlsMobileNumber format. Must be a valid phone number.", "VALIDATION_ERROR");
            }
        } else if (Boolean.FALSE.equals(request.getMls())) {
            request.setMlsMobileNumber(null);
        }

        Land land = landMapper.toEntity(request);
        land.setVendorId(vendorId);
        land.setStatus(LandStatus.PENDING_APPROVAL);
        land.setViewCount(0);
        land.setInquiryCount(0);
        Land saved = landRepository.save(land);
        log.debug("Land created with id: {}", saved.getId());

        // Spend the credit against the saved listing id, so the ledger records which listing it
        // paid for and a retried submission of the same listing cannot spend twice. Deleting,
        // rejecting or expiring the listing does not give the credit back — an admin reversal is
        // the only route, and it is audited.
        userServiceClient.consumeListingCredit(vendorId, saved.getId(), "listing.create:" + saved.getId());

        sendListingStatusEmail(saved, LandStatus.PENDING_APPROVAL, null);
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
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy).and(Sort.by(Sort.Direction.DESC, "id")));
        Page<Land> lands = landRepository.findByStatusAndDeletedFalse(LandStatus.ACTIVE, pageable);
        return getPageResponse(lands, userId);
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> getAllListingsForAdmin(LandStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
        Page<Land> lands = (status != null)
                ? landRepository.findByStatusAndDeletedFalse(status, pageable)
                : landRepository.findByDeletedFalse(pageable);
        return getPageResponse(lands, null);
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> searchLands(String query, int page, int size, UUID userId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
        Specification<Land> spec = LandSpecification.searchLands(query)
                .and(LandSpecification.hasStatus(LandStatus.ACTIVE))
                .and(LandSpecification.isNotDeleted());
        Page<Land> lands = landRepository.findAll(spec, pageable);
        return getPageResponse(lands, userId);
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> filterLands(String city, String q, List<ProjectStage> stages, BigDecimal minPrice, BigDecimal maxPrice, 
                                               BigDecimal minLotSize, BigDecimal maxLotSize, Boolean isFeatured, Boolean isHotDeal,
                                               List<ProjectType> projectTypes, List<BuildingType> buildingTypes, List<ZoningType> zoningTypes, List<ListingType> listingTypes,
                                               List<LandStatus> statuses, Integer forSaleSince, Integer soldSince,
                                               String sortBy, String sortDir,
                                               int page, int size, UUID userId) {
        
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortDir != null) {
            if (sortDir.equalsIgnoreCase("asc")) {
                direction = Sort.Direction.ASC;
            } else if (sortDir.equalsIgnoreCase("desc")) {
                direction = Sort.Direction.DESC;
            }
        }
        
        String sortProperty = "createdAt";
        if (sortBy != null) {
            switch (sortBy.toLowerCase()) {
                case "price", "price_asc", "price_desc" -> {
                    sortProperty = "askingPrice";
                    if (sortBy.equalsIgnoreCase("price_asc")) direction = Sort.Direction.ASC;
                    else if (sortBy.equalsIgnoreCase("price_desc")) direction = Sort.Direction.DESC;
                }
                case "area", "lotsize" -> sortProperty = "lotSize";
                case "createdat", "newest", "oldest" -> {
                    sortProperty = "createdAt";
                    if (sortBy.equalsIgnoreCase("newest")) direction = Sort.Direction.DESC;
                    else if (sortBy.equalsIgnoreCase("oldest")) direction = Sort.Direction.ASC;
                }
            }
        }
        
        Sort primarySort = Sort.by(direction, sortProperty);
        Sort deterministicSort = primarySort.and(Sort.by(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(page, size, deterministicSort);
        
        Specification<Land> spec = Specification.where(LandSpecification.hasStatusesAndSoldSince(statuses, soldSince))
                .and(LandSpecification.isNotDeleted());

        if (city != null && !city.isBlank()) spec = spec.and(LandSpecification.hasCity(city));
        if (q != null && !q.isBlank()) spec = spec.and(LandSpecification.hasKeyword(q));
        if (stages != null && !stages.isEmpty()) spec = spec.and(LandSpecification.hasProjectStages(stages));
        if (minPrice != null) spec = spec.and(LandSpecification.hasMinPrice(minPrice));
        if (maxPrice != null) spec = spec.and(LandSpecification.hasMaxPrice(maxPrice));
        if (minLotSize != null) spec = spec.and(LandSpecification.hasMinLotSize(minLotSize));
        if (maxLotSize != null) spec = spec.and(LandSpecification.hasMaxLotSize(maxLotSize));
        if (isFeatured != null) spec = spec.and(LandSpecification.isFeatured(isFeatured));
        if (isHotDeal != null) spec = spec.and(LandSpecification.isHotDeal(isHotDeal));
        if (projectTypes != null && !projectTypes.isEmpty()) spec = spec.and(LandSpecification.hasProjectTypes(projectTypes));
        if (buildingTypes != null && !buildingTypes.isEmpty()) spec = spec.and(LandSpecification.hasBuildingTypes(buildingTypes));
        if (zoningTypes != null && !zoningTypes.isEmpty()) spec = spec.and(LandSpecification.hasZoningTypes(zoningTypes));
        if (listingTypes != null && !listingTypes.isEmpty()) spec = spec.and(LandSpecification.hasListingTypes(listingTypes));
        if (forSaleSince != null) spec = spec.and(LandSpecification.forSaleSince(forSaleSince));

        boolean isVendorSort = sortBy != null && (
                sortBy.equalsIgnoreCase("rating") || 
                sortBy.equalsIgnoreCase("reviews") || 
                sortBy.equalsIgnoreCase("most_reviews") ||
                sortBy.equalsIgnoreCase("experience") || 
                sortBy.equalsIgnoreCase("most_experience")
        );

        if (isVendorSort) {
            log.debug("Applying in-memory vendor sorting for: {}", sortBy);
            // Fetch all matching items (for small datasets as requested)
            List<Land> allMatching = landRepository.findAll(spec);
            List<LandResponse> allResponses = allMatching.stream()
                    .map(land -> {
                        LandResponse res = landMapper.toResponse(land);
                        boolean isFav = userId != null && favoriteRepository.findByUserIdAndLandId(userId, land.getId()).isPresent();
                        res.setFavorited(isFav);
                        return res;
                    })
                    .collect(Collectors.toList());
            
            enrichWithVendors(allResponses);

            Comparator<BigDecimal> ratingComp = sortDir != null && sortDir.equalsIgnoreCase("asc") 
                    ? Comparator.nullsLast(Comparator.naturalOrder()) 
                    : Comparator.nullsLast(Comparator.reverseOrder());
            Comparator<Integer> reviewsComp = sortDir != null && sortDir.equalsIgnoreCase("asc") 
                    ? Comparator.nullsLast(Comparator.naturalOrder()) 
                    : Comparator.nullsLast(Comparator.reverseOrder());
            Comparator<Integer> experienceComp = sortDir != null && sortDir.equalsIgnoreCase("asc") 
                    ? Comparator.nullsLast(Comparator.naturalOrder()) 
                    : Comparator.nullsLast(Comparator.reverseOrder());
            Comparator<java.time.LocalDateTime> dateComp = sortDir != null && sortDir.equalsIgnoreCase("asc")
                    ? Comparator.nullsLast(Comparator.naturalOrder())
                    : Comparator.nullsLast(Comparator.reverseOrder());

            Comparator<LandResponse> comparator = switch (sortBy.toLowerCase()) {
                case "rating" -> Comparator.comparing(LandResponse::getVendorRating, ratingComp);
                case "reviews", "most_reviews" -> Comparator.comparing(LandResponse::getVendorTotalReviews, reviewsComp);
                case "experience", "most_experience" -> Comparator.comparing(LandResponse::getVendorYearsOfExperience, experienceComp);
                default -> Comparator.comparing(LandResponse::getCreatedAt, dateComp);
            };
            allResponses.sort(comparator);

            int total = allResponses.size();
            int start = page * size;
            int end = Math.min(start + size, total);
            List<LandResponse> pagedContent = (start < total) ? allResponses.subList(start, end) : Collections.emptyList();

            return PageResponse.<LandResponse>builder()
                    .content(pagedContent)
                    .number(page)
                    .size(size)
                    .totalElements((long) total)
                    .totalPages((int) Math.ceil((double) total / size))
                    .first(page == 0)
                    .last(end == total)
                    .build();
        }

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
    public LandResponse updateLandStatus(UUID id, LandStatus status, UUID userId, boolean isAdmin) {
        return updateLandStatus(id, status, userId, isAdmin, null);
    }

    /**
     * Changes a listing's status and notifies the owner.
     *
     * @param rejectionReason reviewer's reason, included verbatim in the rejection email. A
     *                        rejection with no actionable reason leaves the owner with nothing to
     *                        fix, so a fallback is used rather than sending an empty one.
     */
    @Transactional
    public LandResponse updateLandStatus(UUID id, LandStatus status, UUID userId, boolean isAdmin,
                                         String rejectionReason) {
        Land land = landRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", id));
        
        if (!isAdmin) {
            if (status != LandStatus.SOLD) {
                throw new ForbiddenException("Only admins can change status to " + status);
            }
            if (!land.getVendorId().equals(userId)) {
                throw new ForbiddenException("You are not authorized to update this listing's status");
            }
        }
        
        LandStatus oldStatus = land.getStatus();
        land.setStatus(status);
        Land saved = landRepository.save(land);
        if (oldStatus != status) {
            sendListingStatusEmail(saved, status, rejectionReason);
        }
        return getLandResponseWithFavorite(saved, userId);
    }

    @Transactional
    public LandResponse updateLand(UUID id, LandCreateRequest request, UUID userId, boolean isAdmin) {
        Land land = landRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", id));
        if (!isAdmin && !land.getVendorId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to update this listing");
        }
        
        // MLS Validation logic
        if (Boolean.TRUE.equals(request.getMls())) {
            if (request.getMlsMobileNumber() == null || request.getMlsMobileNumber().isBlank()) {
                throw new BadRequestException("mlsMobileNumber is required when mls is true", "VALIDATION_ERROR");
            }
            if (!request.getMlsMobileNumber().matches("^\\+?[0-9\\s\\-\\(\\)]{7,20}$")) {
                throw new BadRequestException("Invalid mlsMobileNumber format. Must be a valid phone number.", "VALIDATION_ERROR");
            }
        } else if (Boolean.FALSE.equals(request.getMls())) {
            request.setMlsMobileNumber(null);
        }

        // Editing a rejected listing is a resubmission: it goes back into review and the owner is
        // told, rather than silently staying rejected while the owner believes they have fixed it.
        boolean resubmitted = land.getStatus() == LandStatus.REJECTED && !isAdmin;
        if (resubmitted) {
            land.setStatus(LandStatus.PENDING_APPROVAL);
        }

        landMapper.updateEntity(request, land);
        Land saved = landRepository.save(land);

        if (resubmitted) {
            // Keyed on the update timestamp so each genuine resubmission mails, while a retry of
            // the same save does not.
            sendListingStatusEmail(saved, LandStatus.PENDING_APPROVAL, null,
                    "listing.resubmitted:" + saved.getId() + ":" + saved.getUpdatedAt());
        }
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
        // Only the key is stored. The previous code assembled a bucket URL from raw environment
        // variables — which produced "https://null.s3.null.amazonaws.com/..." wherever those were
        // unset, and a permanent 403 where they were set, because the bucket is private. The URL
        // clients use is signed at read time by LandMapper.signMediaUrls().
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
        // Key only — see addImageMetadata. Signed at read time.
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
                response.setVendorPhoneNumber(vendor.getPhoneNumber());
                response.setVendorRating(vendor.getRating());
                response.setVendorTotalReviews(vendor.getTotalReviews());
                response.setVendorYearsOfExperience(vendor.getYearsOfExperience());
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
            response.setVendorPhoneNumber(vendor.getPhoneNumber());
            response.setVendorRating(vendor.getRating());
            response.setVendorTotalReviews(vendor.getTotalReviews());
            response.setVendorYearsOfExperience(vendor.getYearsOfExperience());
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

    /**
     * What the user has posted, and what their credit balance allows.
     *
     * <p>The entitlement is the aggregated credit balance from payment-service — every package
     * ever bought, minus every credit spent — not a per-plan cap. The listing counts here are
     * descriptive; they are deliberately not used to compute what is left, because deleting a
     * listing does not return its credit and counting live listings would imply that it does.
     */
    public Map<String, Object> getSlotUsage(UUID userId) {
        long draft = draftRepository.countByOwnerIdAndStatusAndDeletedFalse(userId, com.landgo.coreservice.enums.DraftStatus.IN_PROGRESS);
        long pending = landRepository.countByVendorIdAndStatusAndDeletedFalse(userId, LandStatus.PENDING_APPROVAL);
        long live = landRepository.countActiveListingsByVendorId(userId);
        long total = landRepository.countAllByVendorIdAndDeletedFalse(userId) + draft;

        UserServiceClient.ListingCredits credits = userServiceClient.getListingCredits(userId);
        int purchased = credits != null ? credits.purchased() : 0;
        int used = credits != null ? credits.used() : 0;
        int available = credits != null ? credits.available() : 0;

        Map<String, Object> usage = new LinkedHashMap<>();
        usage.put("draft", draft);
        usage.put("pending", pending);
        usage.put("live", live);
        usage.put("total", total);

        usage.put("creditsPurchased", purchased);
        usage.put("creditsUsed", used);
        usage.put("creditsAvailable", available);
        usage.put("creditsNeverExpire", true);

        // Legacy names, kept so existing clients keep rendering. Both now report the credit
        // balance rather than a plan tier's allowance.
        usage.put("maxListings", purchased);
        usage.put("remainingSlots", available);
        return usage;
    }

    /**
     * Notifies the listing owner that their listing's status changed.
     *
     * <p>Keyed on the listing and the status it moved to, so re-running a transition — an admin
     * re-saving the same decision, or a retried request — mails once. Failures are logged and
     * swallowed: the status change is already committed and must not be rolled back by email.
     */
    private void sendListingStatusEmail(Land land, LandStatus status, String rejectionReason) {
        sendListingStatusEmail(land, status, rejectionReason, null);
    }

    private void sendListingStatusEmail(Land land, LandStatus status, String rejectionReason,
                                        String idempotencyKey) {
        try {
            UserResponse user = userServiceClient.getUserById(land.getVendorId());
            if (user == null || user.getEmail() == null) {
                log.warn("No email on file for vendor {} — skipping listing {} notification",
                        land.getVendorId(), status);
                return;
            }

            Map<String, String> vars = new HashMap<>();
            vars.put("User", user.getFullName());
            vars.put("listingTitle", getListingTitle(land));
            vars.put("listingId", land.getId().toString());
            vars.put("listingAddress", land.getAddress() != null ? land.getAddress() : "");
            vars.put("myListingsUrl", myListingsUrl);
            vars.put("listingUrl", publicListingBaseUrl + "/" + land.getId());
            vars.put("editUrl", myListingsUrl);

            String key = idempotencyKey != null && !idempotencyKey.isBlank()
                    ? idempotencyKey
                    : "listing." + status.name().toLowerCase() + ":" + land.getId();

            switch (status) {
                case PENDING_APPROVAL -> userServiceClient.sendEmail(user.getEmail(),
                        "LandGo - Listing submitted for review", "ListingSubmitted", vars, key);
                case ACTIVE -> userServiceClient.sendEmail(user.getEmail(),
                        "LandGo - Your listing is live", "ListingApproved", vars, key);
                case REJECTED -> {
                    vars.put("rejectionReason", rejectionReason != null && !rejectionReason.isBlank()
                            ? rejectionReason
                            : "Your listing does not yet meet our listing guidelines. "
                                    + "Please review the details and resubmit.");
                    userServiceClient.sendEmail(user.getEmail(),
                            "LandGo - Changes requested on your listing", "ListingRejected", vars, key);
                }
                // SOLD and EXPIRED have no owner-facing template yet; nothing is sent rather than
                // reusing a template that would say the wrong thing.
                default -> log.debug("No owner email defined for listing status {}", status);
            }
        } catch (Exception e) {
            log.error("Failed to send listing {} email for landId: {}", status, land.getId(), e);
        }
    }

    private String getListingTitle(Land land) {
        if (land.getProjectSpecification() != null) {
            Object rawTitle = land.getProjectSpecification().get("title");
            if (rawTitle != null) {
                return rawTitle.toString();
            }
        }
        return "Beautiful Land Listing";
    }
}