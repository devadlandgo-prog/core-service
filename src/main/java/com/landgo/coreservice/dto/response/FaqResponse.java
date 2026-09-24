package com.landgo.coreservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "A published FAQ entry")
public class FaqResponse {

    private UUID id;

    @Schema(example = "How do listing credits work?")
    private String question;

    @Schema(description = "Plain-text answer, always present")
    private String answer;

    @Schema(description = "Sanitized rich-text answer; absent when the FAQ is plain text only")
    private String answerHtml;

    @Schema(example = "Listings")
    private String category;

    @Schema(example = "10")
    private Integer sortOrder;

    private LocalDateTime updatedAt;
}
