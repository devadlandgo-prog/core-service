package com.landgo.coreservice.dto.request;
import com.landgo.coreservice.enums.ProjectStage;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SavedSearchRequest {
    @NotBlank @Size(min = 1, max = 100) private String name;
    @Size(max = 200) private String keyword;
    @Size(max = 100) private String city;
    private ProjectStage projectStage;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minLotSize;
    private BigDecimal maxLotSize;
    @Builder.Default private boolean notificationsEnabled = true;
}
