package com.landgo.coreservice.service;

import com.landgo.coreservice.dto.response.VendorResponse;
import com.landgo.coreservice.dto.response.UserResponse;
import com.landgo.coreservice.dto.request.ProfessionalSubscriptionRequest;
import com.landgo.coreservice.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${app.services.user-service-url:http://localhost:8081}")
    private String userServiceUrl;

    @Value("${app.services.payment-service-url:http://localhost:8082}")
    private String paymentServiceUrl;

    public UserResponse getUserById(UUID userId) {
        try {
            return restTemplate.getForObject(userServiceUrl + "/internal/users/" + userId, UserResponse.class);
        } catch (RestClientException e) {
            log.warn("Failed to fetch user profile from user-service for userId={}: {}", userId, e.getMessage());
            return null;
        }
    }

    public VendorResponse getVendorProfileForUser(UUID userId) {
        try {
            return restTemplate.getForObject(userServiceUrl + "/internal/users/" + userId + "/vendor", VendorResponse.class);
        } catch (RestClientException e) {
            log.warn("Failed to fetch vendor profile from user-service for userId={}: {}", userId, e.getMessage());
            return null;
        }
    }

    public boolean hasActiveSubscription(UUID userId) {
        try {
            Map<?, ?> response = restTemplate.getForObject(
                paymentServiceUrl + "/internal/subscriptions/user/" + userId + "/active", 
                Map.class
            );
            return response != null && Boolean.TRUE.equals(response.get("active"));
        } catch (RestClientException e) {
            log.warn("Failed to check active subscription from payment-service for userId={}: {}", userId, e.getMessage());
            return false;
        }
    }

    public Map<String, String> createProfessionalSubscriptionIntent(UUID userId, ProfessionalSubscriptionRequest request) {
        try {
            Map<String, Object> payload = Map.of(
                    "planId", request.getPlanId(),
                    "subscriptionType", request.getSubscriptionType(),
                    "paymentMethodId", request.getPaymentMethodId(),
                    "autoRenew", request.isAutoRenew()
            );
            @SuppressWarnings("unchecked")
            Map<String, String> response = restTemplate.postForObject(
                    paymentServiceUrl + "/internal/subscriptions/user/" + userId + "/intent",
                    payload,
                    Map.class
            );
            if (response == null || !response.containsKey("paymentIntentId") || !response.containsKey("clientSecret")) {
                throw new BadRequestException("Invalid intent response from payment service");
            }
            return response;
        } catch (RestClientException e) {
            log.error("Failed to create professional subscription intent for userId={}: {}", userId, e.getMessage());
            throw new BadRequestException("Unable to create subscription intent");
        }
    }
}