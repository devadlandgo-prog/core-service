package com.landgo.coreservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalSubscriptionRequest {
    @NotBlank(message = "planId is required")
    private String planId;

    @NotBlank(message = "subscriptionType is required")
    private String subscriptionType;

    @NotBlank(message = "paymentMethodId is required")
    private String paymentMethodId;

    private String email;

    @Builder.Default
    private boolean autoRenew = true;
}
