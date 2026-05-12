package com.landgo.coreservice.service;

import com.landgo.coreservice.entity.Enquiry;
import com.landgo.coreservice.enums.EnquiryStatus;
import com.landgo.coreservice.repository.EnquiryRepository;
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

    @Transactional
    public void createEnquiry(UUID listingId, String senderNameOrEmail, String phone, String message) {
        log.info("Transaction BEGIN: Creating enquiry for listing {}", listingId);
        
        String name = null;
        String email = null;
        
        if (senderNameOrEmail != null && senderNameOrEmail.contains("@")) {
            email = senderNameOrEmail;
            // Optionally set name to the part before @ if we want to be smart
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
}
