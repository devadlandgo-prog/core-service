package com.landgo.coreservice.dto.response;
import lombok.*;
import java.util.List;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PageResponse<T> {
    private List<T> data;
    private int page;
    private int pageSize;
    private long total;
    private int totalPages;
    private boolean first;
    private boolean last;
}
