package com.landgo.coreservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/** Persists a drag-and-drop reorder as explicit {@code sortOrder} values. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaqReorderRequest {

    @NotEmpty(message = "items is required")
    @Valid
    private List<Item> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        @NotNull(message = "id is required")
        private UUID id;

        @NotNull(message = "sortOrder is required")
        @Min(value = 0, message = "sortOrder must be zero or greater")
        private Integer sortOrder;
    }
}
