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

    @org.springframework.beans.factory.annotation.Value("${app.web.listing-base-url:https://landgo.ca/listings}")
    private String publicListingBaseUrl;

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
        Enquiry saved = enquiryRepository.save(enquiry);
        log.info("Transaction COMMIT: Enquiry saved for listing {}", listingId);

        try {
            final String finalName = name;
            final String finalEmail = email;
            landRepository.findByIdAndDeletedFalse(listingId).ifPresent(land -> {
                // The recipient is resolved from server-side listing ownership. The client never
                // supplies the seller's address, and a client-visible owner field is not trusted.
                UserResponse vendor = userServiceClient.getUserById(land.getVendorId());
                if (vendor == null || vendor.getEmail() == null) {
                    log.warn("No owner email for listing {} — inquiry stored but not mailed", listingId);
                    return;
                }

                // Every value below is attacker-controlled and lands in an HTML email, so it is
                // escaped here rather than trusting the template.
                java.util.Map<String, String> vars = new java.util.HashMap<>();
                vars.put("Owner", escapeHtml(vendor.getFullName()));
                vars.put("listingTitle", escapeHtml(getListingTitle(land)));
                vars.put("listingUrl", publicListingBaseUrl + "/" + land.getId());
                vars.put("senderName", escapeHtml(finalName != null ? finalName : "A potential buyer"));
                vars.put("senderEmail", escapeHtml(finalEmail != null ? finalEmail : "N/A"));
                vars.put("senderPhone", escapeHtml(phone != null ? phone : "N/A"));
                vars.put("message", escapeHtml(message != null ? message : ""));

                userServiceClient.sendEmail(vendor.getEmail(), "LandGo - New Inquiry Received",
                        "InquiryReceived", vars, "listing.enquiry:" + saved.getId());
            });
        } catch (Exception e) {
            log.error("Failed to send inquiry email notification for listingId: {}", listingId, e);
        }
    }

    /**
     * Escapes text before it is interpolated into an HTML email body.
     *
     * <p>Enquiry name, phone and message come straight from an unauthenticated form. Newlines are
     * turned into {@code <br>} after escaping so a multi-line message still reads correctly.
     */
    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("\r\n", "<br>")
                .replace("\n", "<br>");
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
