package com.landgo.coreservice.service;

import com.landgo.coreservice.entity.Enquiry;
import com.landgo.coreservice.enums.EnquiryStatus;
import com.landgo.coreservice.repository.EnquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnquiryService {

    private final EnquiryRepository enquiryRepository;

    @Transactional
    public void createEnquiry(UUID listingId, String name, String email, String message) {
        Enquiry enquiry = Enquiry.builder()
                .listingId(listingId)
                .senderName(name)
                .senderEmail(email)
                .message(message)
                .status(EnquiryStatus.PENDING)
                .build();
        enquiryRepository.save(enquiry);
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
        Enquiry enquiry = enquiryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enquiry not found"));
        enquiryRepository.delete(enquiry);
    }
}
