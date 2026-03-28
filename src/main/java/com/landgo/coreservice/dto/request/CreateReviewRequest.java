package com.landgo.coreservice.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateReviewRequest {
    @NotNull @Min(1) @Max(5) private Integer rating;
    @NotBlank @Size(max = 200) private String title;
    @NotBlank @Size(max = 2000) private String content;
}
