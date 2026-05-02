package com.landgo.coreservice.service;

import com.landgo.coreservice.dto.request.LandCreateRequest;
import com.landgo.coreservice.dto.response.LandResponse;
import com.landgo.coreservice.dto.response.PageResponse;
import com.landgo.coreservice.dto.response.VendorResponse;
import com.landgo.coreservice.entity.Land;
import com.landgo.coreservice.enums.LandStatus;
import com.landgo.coreservice.enums.ProjectStage;
import com.landgo.coreservice.exception.BadRequestException;
import com.landgo.coreservice.exception.ForbiddenException;
import com.landgo.coreservice.exception.ResourceNotFoundException;
import com.landgo.coreservice.mapper.LandMapper;
import com.landgo.coreservice.repository.LandRepository;
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
    private final UserServiceClient userServiceClient;
    private final LandMapper landMapper;

    @Transactional
    public LandResponse createLand(UUID userId, LandCreateRequest request) {
        VendorResponse vendor = userServiceClient.getVendorProfileForUser(userId);
        if (vendor == null) {
            throw new BadRequestException("You must register as a vendor/seller first");
        }

        Land land = landMapper.toEntity(request);
        land.setVendorId(vendor.getId());
        land.setStatus(LandStatus.PENDING_APPROVAL);

        Land saved = landRepository.save(land);

        log.info("Land listing created: {} by vendor: {}", saved.getId(), vendor.getId());
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
            throw new ResourceNotFoundException("Vendor profile not found");
        }
        return getVendorLands(vendor.getId(), page, size);
    }

    @Transactional
    public LandResponse updateLand(UUID userId, UUID landId, LandCreateRequest request) {
        Land land = landRepository.findByIdAndDeletedFalse(landId)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", landId));

        VendorResponse vendor = userServiceClient.getVendorProfileForUser(userId);
        if (vendor == null || !land.getVendorId().equals(vendor.getId())) {
            throw new ForbiddenException("You are not authorized to update this listing");
        }

        landMapper.updateEntity(request, land);
        Land updated = landRepository.save(land);
        return landMapper.toResponse(updated);
    }

    @Transactional
    public void deleteLand(UUID userId, UUID landId) {
        Land land = landRepository.findByIdAndDeletedFalse(landId)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", landId));

        VendorResponse vendor = userServiceClient.getVendorProfileForUser(userId);
        if (vendor == null || !land.getVendorId().equals(vendor.getId())) {
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

    private PageResponse<LandResponse> buildPageResponse(Page<Land> page) {
        List<LandResponse> content = page.getContent().stream().map(landMapper::toResponse).collect(Collectors.toList());
        return PageResponse.<LandResponse>builder()
                .content(content).pageNumber(page.getNumber()).pageSize(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .first(page.isFirst()).last(page.isLast()).build();
    }
}
