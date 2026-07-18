package com.landgo.coreservice.service;

import com.landgo.coreservice.entity.Enquiry;
import com.landgo.coreservice.entity.Land;
import com.landgo.coreservice.enums.EnquiryStatus;
import com.landgo.coreservice.repository.EnquiryRepository;
import com.landgo.coreservice.repository.LandRepository;
import com.landgo.coreservice.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnquiryService {

    private final EnquiryRepository enquiryRepository;
    private final LandRepository landRepository;
    private final UserServiceClient userServiceClient;

    @Transactional
    public void createEnquiry(UUID listingId, String senderNameOrEmail, String phone, String message) {
        log.info("Transaction BEGIN: Creating enquiry for listing {}", listingId);
        
        String name = null;
        String email = null;
        
        if (senderNameOrEmail != null && senderNameOrEmail.contains("@")) {
            email = senderNameOrEmail;
            name = senderNameOrEmail.split("@")[0];
        } else {
            name = senderNameOrEmail;
        }

        Enquiry enquiry = Enquiry.builder()
                .listingId(listingId)
                .senderName(name)
                .senderEmail(email)
                .senderPhone(phone)
                .message(message)
                .status(EnquiryStatus.PENDING)
                .build();
        enquiryRepository.save(enquiry);
        log.info("Transaction COMMIT: Enquiry saved for listing {}", listingId);

        try {
            final String finalName = name;
            final String finalEmail = email;
            landRepository.findByIdAndDeletedFalse(listingId).ifPresent(land -> {
                UserResponse vendor = userServiceClient.getUserById(land.getVendorId());
                if (vendor != null) {
                    java.util.Map<String, String> vars = new java.util.HashMap<>();
                    vars.put("Owner", vendor.getFullName());
                    vars.put("listingTitle", getListingTitle(land));
                    vars.put("senderName", finalName != null ? finalName : "A potential buyer");
                    vars.put("senderEmail", finalEmail != null ? finalEmail : "N/A");
                    vars.put("senderPhone", phone != null ? phone : "N/A");
                    vars.put("message", message != null ? message : "");
                    userServiceClient.sendEmail(vendor.getEmail(), "LandGo - New Inquiry Received", "InquiryReceived", vars);
                }
            });
        } catch (Exception e) {
            log.error("Failed to send inquiry email notification for listingId: {}", listingId, e);
        }
    }

    public List<Enquiry> getAllEnquiries() {
        return enquiryRepository.findAll();
    }

    public Enquiry getEnquiryById(UUID id) {
        return enquiryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enquiry not found"));
    }

    @Transactional
    public void updateStatus(UUID id, EnquiryStatus status) {
        Enquiry enquiry = getEnquiryById(id);
        enquiry.setStatus(status);
        enquiryRepository.save(enquiry);
    }

    @Transactional
    public void deleteEnquiry(UUID id) {
        log.info("Transaction BEGIN: Deleting enquiry {}", id);
        Enquiry enquiry = enquiryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enquiry not found"));
        enquiryRepository.delete(enquiry);
        log.info("Transaction COMMIT: Enquiry {} deleted", id);
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
