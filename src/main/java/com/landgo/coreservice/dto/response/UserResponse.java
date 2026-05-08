package com.landgo.coreservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String firstName;
    private String lastName;
    @JsonProperty("phone")
    private String phoneNumber;
    private String role;
    private Boolean emailVerified;
    private String profileImageUrl;
}
