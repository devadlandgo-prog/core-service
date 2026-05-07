package com.landgo.coreservice.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateReviewRequest {
    @NotNull @Min(1) @Max(5) private Integer rating;
    @NotBlank @Size(max = 100) private String title;
    @NotBlank @Size(min = 20, max = 1000) private String content;
    private java.util.List<String> tags;
    private java.util.List<String> photos;
}
