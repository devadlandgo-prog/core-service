package com.landgo.coreservice.service;

import com.landgo.coreservice.dto.request.VendorRegisterRequest;
import com.landgo.coreservice.dto.response.PageResponse;
import com.landgo.coreservice.dto.response.VendorResponse;
import com.landgo.coreservice.entity.ProfessionalEnquiry;
import com.landgo.coreservice.entity.VendorProfile;
import com.landgo.coreservice.exception.ResourceNotFoundException;
import com.landgo.coreservice.mapper.VendorMapper;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final VendorMapper vendorMapper;
    private final UserServiceClient userServiceClient;
    private final com.landgo.coreservice.repository.ProfessionalEnquiryRepository professionalEnquiryRepository;
    private final com.landgo.coreservice.repository.LandRepository landRepository;
    private final com.landgo.coreservice.repository.ExpertiseOptionRepository expertiseOptionRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getVendorDashboard(UUID userId) {
        VendorProfile vendor = vendorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));

        long activeListings = landRepository.countActiveListingsByVendorId(vendor.getId());
        long totalViews = landRepository.sumViewCountByVendorId(vendor.getId());
        long totalEnquiries = professionalEnquiryRepository.countByVendorId(vendor.getId());

        List<ProfessionalEnquiry> recentEnquiries = professionalEnquiryRepository.findByVendorIdOrderByCreatedAtDesc(vendor.getId())
                .stream().limit(5).collect(Collectors.toList());

        return Map.of(
                "stats", Map.of(
                        "inquiries", totalEnquiries,
                        "profileViews", totalViews,
                        "totalProjects", activeListings
                ),
                "recentInquiries", recentEnquiries
        );
    }

    @Transactional
    public UUID sendInquiry(Map<String, String> request) {
        UUID vendorId = UUID.fromString(request.get("vendorId"));
        ProfessionalEnquiry enquiry = ProfessionalEnquiry.builder()
                .vendorId(vendorId)
                .senderName(request.get("name"))
                .senderEmail(request.get("email"))
                .message(request.get("message"))
                .build();
        return professionalEnquiryRepository.save(enquiry).getId();
    }

    @Transactional(readOnly = true)
    public List<String> getExpertiseOptions() {
        return expertiseOptionRepository.findByIsActiveTrue().stream()
                .map(com.landgo.coreservice.entity.ExpertiseOption::getName)
                .collect(Collectors.toList());
    }

    @Transactional
    public com.landgo.coreservice.entity.ExpertiseOption createExpertiseOption(String name) {
        com.landgo.coreservice.entity.ExpertiseOption option = com.landgo.coreservice.entity.ExpertiseOption.builder()
                .name(name)
                .isActive(true)
                .build();
        return expertiseOptionRepository.save(option);
    }

    @Transactional
    public void updateExpertiseOption(UUID id, String name) {
        com.landgo.coreservice.entity.ExpertiseOption option = expertiseOptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expertise option not found"));
        option.setName(name);
        expertiseOptionRepository.save(option);
    }

    @Transactional
    public void deleteExpertiseOption(UUID id) {
        com.landgo.coreservice.entity.ExpertiseOption option = expertiseOptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expertise option not found"));
        option.setActive(false);
        expertiseOptionRepository.save(option);
    }

    @Transactional
    public void updateVendorExpertise(UUID userId, List<String> expertise) {
        VendorProfile vendor = vendorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));
        vendor.setSpecialization(expertise);
        vendorRepository.save(vendor);
    }

    @Transactional
    public Map<String, String> createProfessionalSubscriptionIntent(UUID userId) {
        return userServiceClient.createProfessionalSubscriptionIntent(userId);
    }

    @Transactional(readOnly = true)
    public VendorResponse getVendorById(UUID vendorId, UUID currentUserId) {
        VendorProfile vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "id", vendorId));
        return vendorMapper.toResponse(vendor);
    }

    @Transactional(readOnly = true)
    public PageResponse<VendorResponse> getVerifiedVendors(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("companyName").ascending());
        Page<VendorProfile> vendors = vendorRepository.findByVerifiedTrueAndDeletedFalse(pageable);
        return buildPageResponse(vendors);
    }

    @Transactional(readOnly = true)
    public PageResponse<VendorResponse> searchVendors(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("companyName").ascending());
        Page<VendorProfile> vendors = vendorRepository.searchVendors(query, pageable);
        return buildPageResponse(vendors);
    }

    @Transactional
    public VendorResponse registerProfessional(UUID userId, VendorRegisterRequest request) {
        com.landgo.coreservice.entity.User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        VendorProfile vendor = vendorRepository.findByUserId(userId)
                .orElse(VendorProfile.builder().user(user).build());

        vendor.setCompanyName(request.getCompanyName());
        vendor.setCompanyDescription(request.getCompanyDescription());
        vendor.setCompanyLogo(request.getCompanyLogo());
        vendor.setLicenseNumber(request.getLicenseNumber());
        vendor.setSpecialization(request.getSpecialization());
        vendor.setYearsOfExperience(request.getYearsOfExperience());
        vendor.setPhoneNumber(request.getPhoneNumber());
        vendor.setServiceArea(request.getServiceArea());
        vendor.setBio(request.getBio());
        vendor.setCertifications(request.getCertifications());
        vendor.setBusinessAddress(request.getBusinessAddress());
        vendor.setBusinessCity(request.getBusinessCity());
        vendor.setBusinessState(request.getBusinessState());
        vendor.setBusinessZipCode(request.getBusinessZipCode());
        vendor.setBusinessCountry(request.getBusinessCountry());
        vendor.setWebsite(request.getWebsite());

        vendor = vendorRepository.save(vendor);
        return vendorMapper.toResponse(vendor);
    }

    private PageResponse<VendorResponse> buildPageResponse(Page<VendorProfile> page) {
        List<VendorResponse> content = page.getContent().stream().map(vendorMapper::toResponse).collect(Collectors.toList());
        return PageResponse.<VendorResponse>builder()
                .content(content).number(page.getNumber()).size(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .first(page.isFirst()).last(page.isLast()).build();
    }
}
