package com.landgo.coreservice.service;

import com.landgo.coreservice.dto.response.VendorListingStatsResponse;
import com.landgo.coreservice.repository.EnquiryRepository;
import com.landgo.coreservice.repository.LandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VendorListingStatsService {

    private final LandRepository landRepository;
    private final EnquiryRepository enquiryRepository;

    public VendorListingStatsResponse getStatsForVendor(UUID vendorId) {
        long active = landRepository.countActiveListingsByVendorId(vendorId);
        long total = landRepository.countAllByVendorIdAndDeletedFalse(vendorId);
        long views = landRepository.sumViewCountByVendorId(vendorId);
        long enquiries = enquiryRepository.countEnquiriesForVendorListings(vendorId);
        return VendorListingStatsResponse.builder()
                .activeListings(active)
                .totalListings(total)
                .totalViews(views)
                .totalEnquiries(enquiries)
                .build();
    }
}
