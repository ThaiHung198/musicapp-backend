// backend/src/main/java/com/musicapp/backend/controller/ChartController.java

package com.musicapp.backend.controller;

import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.ChartSongDto;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.service.ChartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chart")
@RequiredArgsConstructor
public class ChartController {

    private final ChartService chartService;

    @GetMapping
    public ResponseEntity<BaseResponse<PagedResponse<ChartSongDto>>> getChart(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal User currentUser) {

        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<ChartSongDto> chartPage = chartService.getChart(pageable, currentUser);
        PagedResponse<ChartSongDto> response = PagedResponse.of(chartPage.getContent(), chartPage);
        return ResponseEntity.ok(BaseResponse.success("Chart fetched successfully", response));
    }
}