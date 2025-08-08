package com.musicapp.backend.controller;

import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.singer.AdminCreateSingerRequest;
import com.musicapp.backend.dto.singer.CreateSingerRequest;
import com.musicapp.backend.dto.singer.SingerDto;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.service.SingerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/singers")
@RequiredArgsConstructor
public class SingerController {

    private final SingerService singerService;

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<PagedResponse<SingerDto>>> getAllSingersForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size);
        Page<SingerDto> singers = singerService.getAllSingersForAdmin(search, pageable);
        PagedResponse<SingerDto> response = PagedResponse.of(singers.getContent(), singers);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PostMapping(value = "/admin", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<SingerDto>> createSingerByAdmin(
            @RequestPart("singerRequest") @Valid AdminCreateSingerRequest request,
            @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile) {
        SingerDto newSinger = singerService.createSingerByAdmin(request, avatarFile);
        return ResponseEntity.ok(BaseResponse.success("Admin created singer successfully", newSinger));
    }

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