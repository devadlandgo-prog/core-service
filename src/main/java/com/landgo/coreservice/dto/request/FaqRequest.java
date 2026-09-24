package com.landgo.coreservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaqRequest {

    @NotBlank(message = "Question is required")
    @Size(max = 200, message = "Question must be at most 200 characters")
    private String question;

    /**
     * Plain-text answer. Optional only when {@code answerHtml} is supplied — the service derives
     * the plain text from the markup in that case.
     */
    @Size(max = 10000, message = "Answer must be at most 10000 characters")
    private String answer;

    @Size(max = 10000, message = "Answer HTML must be at most 10000 characters")
    private String answerHtml;

    @Size(max = 100, message = "Category must be at most 100 characters")
    private String category;

    @Min(value = 0, message = "sortOrder must be zero or greater")
    private Integer sortOrder;
}
