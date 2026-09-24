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

    /**
     * Aggregated land-listing credit entitlement from payment-service.
     *
     * <p>Credits, not a plan-tier cap: a user who has bought three packages has three packages'
     * worth of credits, which the old plan-derived {@code maxListings} could not express.
     *
     * @return purchased/used/available counts, or null when payment-service is unreachable
     */
    public ListingCredits getListingCredits(UUID userId) {
        try {
            Map<?, ?> response = restTemplate.getForObject(
                    paymentServiceUrl + "/internal/listing-credits/user/" + userId, Map.class);
            if (response == null) {
                return null;
            }
            return new ListingCredits(
                    intValue(response.get("creditsPurchased")),
                    intValue(response.get("creditsUsed")),
                    intValue(response.get("creditsAvailable")));
        } catch (RestClientException e) {
            log.warn("Failed to fetch listing credits for userId={}: {}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * Spends one listing credit.
     *
     * <p>The spend happens in payment-service under a conditional update, so two listings
     * submitted at the same moment cannot both take the last credit.
     *
     * @param idempotencyKey reuse across retries of the same submission so it is spent once
     * @throws BadRequestException when the user has no credit left
     */
    public void consumeListingCredit(UUID userId, UUID listingId, String idempotencyKey) {
        String url = paymentServiceUrl + "/internal/listing-credits/user/" + userId + "/consume"
                + "?idempotencyKey=" + java.net.URLEncoder.encode(idempotencyKey, java.nio.charset.StandardCharsets.UTF_8)
                + (listingId != null ? "&listingId=" + listingId : "");
        try {
            restTemplate.postForObject(url, null, Map.class);
        } catch (org.springframework.web.client.HttpClientErrorException.Conflict e) {
            throw new BadRequestException(
                    "You have no listing credits left. Buy a land listing package to post another listing.",
                    "NO_LISTING_CREDITS");
        } catch (RestClientException e) {
            // Failing closed: granting a listing for free because billing is unreachable is worse
            // than asking the user to retry.
            log.error("Could not spend a listing credit for userId={}: {}", userId, e.getMessage());
            throw new BadRequestException(
                    "Could not verify your listing credits right now. Please try again shortly.",
                    "LISTING_CREDITS_UNAVAILABLE");
        }
    }

    private int intValue(Object raw) {
        return raw instanceof Number number ? number.intValue() : 0;
    }

    /** Aggregated credit counts for one user. */
    public record ListingCredits(int purchased, int used, int available) {}

    /**
     * @deprecated superseded by {@link #getListingCredits(UUID)}. This reported a plan-tier cap
     *             that ignored repeat purchases entirely.
     */
    @Deprecated
    public Integer getUserMaxListings(UUID userId) {
        try {
            Map<?, ?> response = restTemplate.getForObject(
                paymentServiceUrl + "/internal/subscriptions/user/" + userId + "/plan",
                Map.class
            );
            if (response != null && response.containsKey("data")) {
                Map<?, ?> data = (Map<?, ?>) response.get("data");
                if (data != null && data.containsKey("maxListings")) {
                    Object maxListings = data.get("maxListings");
                    if (maxListings instanceof Integer) {
                        return (Integer) maxListings;
                    }
                }
            }
            // Return null if no subscription or maxListings not set (unlimited)
            return null;
        } catch (RestClientException e) {
            log.warn("Failed to fetch maxListings from payment-service for userId={}: {}", userId, e.getMessage());
            return null;
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

    @Value("${app.mail.logo-url:https://landgo.app/logo_with_tagline.png}")
    private String logoUrl;

    /**
     * @deprecated use {@link #sendEmail(String, String, String, Map, String)} — an email with no
     *             idempotency key is re-sent on every retry of the transition that caused it.
     */
    @Deprecated
    public void sendEmail(String toEmail, String subject, String templateName, java.util.Map<String, String> variables) {
        sendEmail(toEmail, subject, templateName, variables, null);
    }

    /**
     * Queues one transactional email.
     *
     * <p>{@code idempotencyKey} identifies the committed change that caused it — a listing id plus
     * its new status, say — so re-running a transition mails once. Never throws: a mail failure
     * must not roll back the listing change that triggered it.
     */
    public void sendEmail(String toEmail, String subject, String templateName,
                          java.util.Map<String, String> variables, String idempotencyKey) {
        try {
            String htmlBody = renderTemplate(templateName, variables);

            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("toEmail", toEmail);
            payload.put("subject", subject);
            payload.put("htmlBody", htmlBody);
            payload.put("templateName", templateName);
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                payload.put("idempotencyKey", idempotencyKey);
            }

            restTemplate.postForObject(userServiceUrl + "/internal/users/email/send", payload, Void.class);
            log.info("Queued internal HTML email for template {} (key={})", templateName, idempotencyKey);
        } catch (Exception e) {
            log.error("Failed to render/send internal email request for template {}: {}", templateName, e.getMessage());
        }
    }

    private String renderTemplate(String templateName, java.util.Map<String, String> variables) throws java.io.IOException {
        String templatePath = "email-templates/" + templateName + ".html";
        org.springframework.core.io.ClassPathResource resource = new org.springframework.core.io.ClassPathResource(templatePath);
        if (!resource.exists()) {
            throw new IllegalArgumentException("Template file not found: " + templatePath);
        }
        String template = new String(resource.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);

        // Inject logoUrl
        template = template.replace("/static/icon.svg", logoUrl);
        template = template.replace("{{logoUrl}}", logoUrl);

        if (variables != null) {
            for (java.util.Map.Entry<String, String> entry : variables.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue() != null ? entry.getValue() : "";
                template = template.replace("<!-- -->" + key + "<!-- -->", value);
                template = template.replace("{{" + key + "}}", value);
                template = template.replace("${" + key + "}", value);
            }
        }
        return template;
    }
}