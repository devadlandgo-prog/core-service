package com.landgo.coreservice.config;

import com.landgo.coreservice.entity.Enquiry;
import com.landgo.coreservice.repository.EnquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DatabaseWriteHealthIndicator implements HealthIndicator {

    private final EnquiryRepository enquiryRepository;

    @Override
    public Health health() {
        try {
            Enquiry testEnquiry = Enquiry.builder()
                    .senderName("HEALTH_CHECK")
                    .senderEmail("health@landgo.com")
                    .message("Health check write test")
                    .listingId(UUID.randomUUID())
                    .build();
            
            Enquiry saved = enquiryRepository.save(testEnquiry);
            enquiryRepository.deleteById(saved.getId());
            
            return Health.up()
                    .withDetail("database", "Persistence verified")
                    .withDetail("writeCapability", "Functional")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "Persistence failure")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
