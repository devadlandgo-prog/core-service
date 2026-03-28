package com.landgo.coreservice.service;

import com.landgo.coreservice.dto.response.PageResponse;
import com.landgo.coreservice.dto.response.VendorResponse;
import com.landgo.coreservice.entity.VendorProfile;
import com.landgo.coreservice.exception.ForbiddenException;
import com.landgo.coreservice.exception.ResourceNotFoundException;
import com.landgo.coreservice.mapper.VendorMapper;
import com.landgo.coreservice.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;
    private final VendorMapper vendorMapper;
    private final UserServiceClient userServiceClient;

    @Transactional(readOnly = true)
    public VendorResponse getVendorById(UUID vendorId, UUID currentUserId) {
        VendorProfile vendor = vendorRepository.findByIdWithUser(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));

        if (currentUserId != null) {
            if (!userServiceClient.hasActiveSubscription(currentUserId)) {
                throw new ForbiddenException("Subscription required to view vendor details");
            }
        }

        return vendorMapper.toResponse(vendor);
    }

    @Transactional(readOnly = true)
    public VendorResponse getMyVendorProfile(UUID userId) {
        VendorProfile vendor = vendorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found for this user"));
        return vendorMapper.toResponse(vendor);
    }

    @Transactional(readOnly = true)
    public PageResponse<VendorResponse> getVerifiedVendors(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<VendorProfile> vendors = vendorRepository.findByVerifiedTrueAndDeletedFalse(pageable);
        return buildPageResponse(vendors);
    }

    @Transactional(readOnly = true)
    public PageResponse<VendorResponse> searchVendors(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<VendorProfile> vendors = vendorRepository.searchVendors(search, pageable);
        return buildPageResponse(vendors);
    }

    private PageResponse<VendorResponse> buildPageResponse(Page<VendorProfile> page) {
        List<VendorResponse> content = page.getContent().stream().map(vendorMapper::toResponse).collect(Collectors.toList());
        return PageResponse.<VendorResponse>builder()
                .content(content).pageNumber(page.getNumber()).pageSize(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .first(page.isFirst()).last(page.isLast()).build();
    }
}
