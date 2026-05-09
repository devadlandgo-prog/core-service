package com.landgo.coreservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EnquiryRequest {
    
    @NotBlank(message = "Sender name is required")
    private String senderName;
    
    @NotBlank(message = "Sender email is required")
    @Email(message = "Valid email is required")
    private String senderEmail;
    
    private String senderPhone;
    
    @NotBlank(message = "Message is required")
    private String message;
}