package com.musicapp.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> content;
    private PageInfo pageInfo;
    
    public static <T> PagedResponse<T> of(List<T> content, org.springframework.data.domain.Page<?> page) {
        return PagedResponse.<T>builder()
                .content(content)
                .pageInfo(new PageInfo(page))
                .build();
    }
}
