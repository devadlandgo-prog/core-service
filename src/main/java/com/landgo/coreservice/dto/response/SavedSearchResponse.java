package com.landgo.coreservice.dto.response;
import com.landgo.coreservice.enums.ProjectStage;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SavedSearchResponse {
    private UUID id;
    private String name;
    private String keyword;
    private String city;
    private ProjectStage projectStage;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minLotSize;
    private BigDecimal maxLotSize;
    private boolean notificationsEnabled;
    private int matchCount;
    private LocalDateTime lastNotifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
