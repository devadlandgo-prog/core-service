package com.landgo.coreservice.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.Map;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DraftStepRequest {
    @NotBlank(message = "Step is required") private String step;
    @NotNull(message = "Step data is required") private Map<String, Object> data;
}
