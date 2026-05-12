package com.landgo.coreservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EnquiryRequest {
    
    @NotBlank(message = "Sender name or email is required")
    private String senderNameOrEmail;
    
    private String senderPhone;
    
    @NotBlank(message = "Message is required")
    private String message;
}