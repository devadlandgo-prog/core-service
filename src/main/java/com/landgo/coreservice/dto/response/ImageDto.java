package com.landgo.coreservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageDto {
    private String id; // Typically the file key or a UUID
    private String url;
    private String fileName;
    private boolean isPrimary;
    private LocalDateTime uploadedAt;
}
