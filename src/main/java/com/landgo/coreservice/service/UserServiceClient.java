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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @SuppressWarnings("unchecked")
    public Map<UUID, VendorResponse> getVendorProfilesBatch(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) return Map.of();
        try {
            String ids = userIds.stream().map(UUID::toString).collect(Collectors.joining(","));
            String url = userServiceUrl + "/internal/users/vendors/batch?userIds=" + ids;
            
            ResponseEntity<Map<UUID, VendorResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<UUID, VendorResponse>>() {}
            );
            return response.getBody() != null ? response.getBody() : Map.of();
        } catch (RestClientException e) {
            log.warn("Failed to fetch vendor profiles batch from user-service: {}", e.getMessage());
            return Map.of();
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
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("planId", request.getPlanId());
            payload.put("subscriptionType", request.getSubscriptionType());
            payload.put("paymentMethodId", request.getPaymentMethodId());
            payload.put("autoRenew", request.isAutoRenew());
            payload.put("email", request.getEmail());
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