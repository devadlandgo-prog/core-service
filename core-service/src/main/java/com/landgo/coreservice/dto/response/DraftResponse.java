package com.landgo.coreservice.dto.response;
import com.landgo.coreservice.enums.DraftStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DraftResponse {
    private UUID draftId;
    private DraftStatus status;
    private Integer currentStep;
    private Map<String, Object> step1;
    private Map<String, Object> step2;
    private Map<String, Object> step3;
    private Map<String, Object> step4;
    private Map<String, Object> step5;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
