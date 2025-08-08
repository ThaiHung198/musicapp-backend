package com.musicapp.backend.controller;

import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.singer.CreateSingerRequest;
import com.musicapp.backend.dto.singer.SingerDto;
import com.musicapp.backend.dto.singer.AdminCreateSingerRequest;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.service.SingerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/singers")
@RequiredArgsConstructor
public class SingerController {

    private final SingerService singerService;

    @GetMapping("/selectable")
    @PreAuthorize("hasRole('CREATOR')")
    public ResponseEntity<BaseResponse<List<SingerDto>>> getSelectableSingers(
            @AuthenticationPrincipal User creator
    ) {
        List<SingerDto> singers = singerService.getSelectableSingersForCreator(creator);
        return ResponseEntity.ok(BaseResponse.success(singers));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<PagedResponse<SingerDto>>> getAllSingers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size);
        Page<SingerDto> singers;

        if (search != null && !search.trim().isEmpty()) {
            singers = singerService.searchSingers(search.trim(), pageable);
        } else {
            singers = singerService.getAllSingers(pageable);
        }

        PagedResponse<SingerDto> response = PagedResponse.of(singers.getContent(), singers);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/list")
    public ResponseEntity<BaseResponse<List<SingerDto>>> getAllSingersAsList() {
        List<SingerDto> singers = singerService.getAllSingersAsList();
        return ResponseEntity.ok(BaseResponse.success(singers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<SingerDto>> getSingerById(@PathVariable Long id) {
        SingerDto singer = singerService.getSingerById(id);
        return ResponseEntity.ok(BaseResponse.success(singer));
    }

    /**
     * Endpoint dành riêng cho Admin để tạo ca sĩ mới.
     * Ca sĩ được tạo sẽ có status APPROVED và không thuộc NPT nào.
     */
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<SingerDto>> createSingerByAdmin(
            @Valid @RequestBody AdminCreateSingerRequest request) {
        SingerDto newSinger = singerService.createSingerByAdmin(request);
        return ResponseEntity.ok(BaseResponse.success("Admin created singer successfully", newSinger));
    }

    // Endpoint cũ này Tú giữ lại để tương thích với các logic khác
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<SingerDto>> createSinger(
            @Valid @RequestBody CreateSingerRequest request,
            Authentication authentication) {
        String creatorUsername = authentication.getName();
        SingerDto singer = singerService.createSinger(request, creatorUsername);
        return ResponseEntity.ok(BaseResponse.success("Singer created successfully", singer));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<SingerDto>> updateSinger(
            @PathVariable Long id,
            @Valid @RequestBody CreateSingerRequest request) {
        SingerDto singer = singerService.updateSinger(id, request);
        return ResponseEntity.ok(BaseResponse.success("Singer updated successfully", singer));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteSinger(@PathVariable Long id) {
        singerService.deleteSinger(id);
        return ResponseEntity.ok(BaseResponse.success("Singer deleted successfully", null));
    }
}