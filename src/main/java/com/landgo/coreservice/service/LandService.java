package com.landgo.coreservice.service;

import com.landgo.coreservice.dto.request.LandCreateRequest;
import com.landgo.coreservice.dto.response.LandResponse;
import com.landgo.coreservice.dto.response.PageResponse;
import com.landgo.coreservice.entity.Land;
import com.landgo.coreservice.entity.User;
import com.landgo.coreservice.entity.VendorProfile;
import com.landgo.coreservice.enums.LandStatus;
import com.landgo.coreservice.enums.ProjectStage;
import com.landgo.coreservice.exception.BadRequestException;
import com.landgo.coreservice.exception.ForbiddenException;
import com.landgo.coreservice.exception.ResourceNotFoundException;
import com.landgo.coreservice.mapper.LandMapper;
import com.landgo.coreservice.repository.LandRepository;
import com.landgo.coreservice.repository.UserRepository;
import com.landgo.coreservice.repository.VendorRepository;
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
    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final LandMapper landMapper;

    @Transactional
    public LandResponse createLand(UUID userId, LandCreateRequest request) {
        VendorProfile vendor = vendorRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("You must register as a vendor/seller first"));

        Land land = landMapper.toEntity(request);
        land.setVendor(vendor);
        land.setStatus(LandStatus.PENDING_APPROVAL);

        Land saved = landRepository.save(land);
        vendor.incrementLandsListed();
        vendorRepository.save(vendor);

        log.info("Land listing created: {} by vendor: {}", saved.getId(), vendor.getId());
        return landMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LandResponse getLandById(UUID id) {
        Land land = landRepository.findByIdWithVendor(id)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", id));
        return landMapper.toResponse(land);
    }

    @Transactional
    public LandResponse getLandByIdWithView(UUID id) {
        Land land = landRepository.findByIdWithVendor(id)
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
        VendorProfile vendor = vendorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));
        return getVendorLands(vendor.getId(), page, size);
    }

    @Transactional
    public LandResponse updateLand(UUID userId, UUID landId, LandCreateRequest request) {
        Land land = landRepository.findByIdWithVendor(landId)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", landId));

        if (!land.getVendor().getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to update this listing");
        }

        landMapper.updateEntity(request, land);
        Land updated = landRepository.save(land);
        return landMapper.toResponse(updated);
    }

    @Transactional
    public void deleteLand(UUID userId, UUID landId) {
        Land land = landRepository.findByIdWithVendor(landId)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", landId));

        if (!land.getVendor().getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to delete this listing");
        }

        land.setDeleted(true);
        landRepository.save(land);
        log.info("Land listing deleted: {} by user: {}", landId, userId);
    }

    @Transactional
    public LandResponse updateLandStatus(UUID landId, LandStatus status) {
        Land land = landRepository.findByIdWithVendor(landId)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", landId));
        land.setStatus(status);
        if (status == LandStatus.SOLD) {
            land.getVendor().incrementLandsSold();
            vendorRepository.save(land.getVendor());
        }
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
