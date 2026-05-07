package com.landgo.coreservice.dto.response;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReviewResponse {
    private UUID id;
    private UUID professionalId;
    private UUID authorId;
    private String authorName;
    private String authorAvatarUrl;
    private Integer rating;
    private String title;
    private String content;
    private java.util.List<String> tags;
    private java.util.List<String> photos;
    private boolean verifiedPurchase;
    private LocalDateTime createdAt;
}
