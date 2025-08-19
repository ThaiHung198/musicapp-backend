package com.musicapp.backend.controller;

import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.ChartSongDto;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.service.ChartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chart")
@RequiredArgsConstructor
public class ChartController {

    private final ChartService chartService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<ChartSongDto>>> getChart(@AuthenticationPrincipal User currentUser) {
        List<ChartSongDto> chart = chartService.getChart(currentUser);
        return ResponseEntity.ok(BaseResponse.success("Chart fetched successfully", chart));
    }
}