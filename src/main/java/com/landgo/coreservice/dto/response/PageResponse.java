package com.landgo.coreservice.dto.response;

import lombok.*;
import java.util.List;
import java.util.Collections;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int number;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public static <T> PageResponse<T> empty(int page, int size) {
        return PageResponse.<T>builder()
                .content(Collections.emptyList())
                .number(page)
                .size(size)
                .totalElements(0)
                .totalPages(0)
                .first(page == 0)
                .last(true)
                .build();
    }
}
