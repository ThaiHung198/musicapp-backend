// File: src/main/java/com/musicapp/backend/controller/SingerController.java
package com.musicapp.backend.controller;

import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.singer.*;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.service.SingerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.musicapp.backend.entity.Singer;

import java.util.List;

@RestController
@RequestMapping("/api/v1/singers")
@RequiredArgsConstructor
public class SingerController {

    private final SingerService singerService;

    @GetMapping("/top")
    @PreAuthorize("permitAll()")
    public ResponseEntity<BaseResponse<List<SingerDto>>> getTopSingers(
            @RequestParam(defaultValue = "8") int limit) {
        List<SingerDto> singers = singerService.getTopSingers(limit);
        return ResponseEntity.ok(BaseResponse.success(singers));
    }

    @GetMapping("/search")
    @PreAuthorize("permitAll()")
    public ResponseEntity<BaseResponse<Page<SingerDto>>> searchSingers(
            @RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<SingerDto> singers = singerService.searchSingers(keyword, pageable);
        return ResponseEntity.ok(BaseResponse.success(singers));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<PagedResponse<SingerDto>>> getAllSingersForAdmin(
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Singer.SingerStatus status) {

        Page<SingerDto> singers = singerService.getAllSingersForAdmin(search, pageable, status);
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

    @PostMapping(value = "/admin/batch", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<List<SingerDto>>> createMultipleSingersByAdmin(
            @RequestPart("singersRequest") @Valid AdminCreateMultipleSingersRequest request,
            @RequestPart(value = "avatarFiles", required = false) List<MultipartFile> avatarFiles,
            @AuthenticationPrincipal User admin
    ) {
        List<SingerDto> newSingers = singerService.createMultipleSingersByAdmin(request, avatarFiles, admin);
        return ResponseEntity.ok(BaseResponse.success("Successfully created " + newSingers.size() + " singers.", newSingers));
    }
    // END-CHANGE

    @PutMapping(value = "/admin/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<SingerDto>> updateSingerByAdmin(
            @PathVariable Long id,
            @RequestPart("singerRequest") @Valid AdminUpdateSingerRequest request,
            @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile) {
        SingerDto updatedSinger = singerService.updateSingerByAdmin(id, request, avatarFile);
        return ResponseEntity.ok(BaseResponse.success("Cập nhật ca sĩ thành công!", updatedSinger));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteSingerByAdmin(@PathVariable Long id) {
        singerService.deleteSingerByAdmin(id);
        return ResponseEntity.ok(BaseResponse.success("Đã xóa ca sĩ thành công.", null));
    }

    @GetMapping("/selectable")
    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<List<SingerDto>>> getSelectableSingers(
            @AuthenticationPrincipal User user
    ) {
        List<SingerDto> singers = singerService.getSelectableSingersForCreator(user);
        return ResponseEntity.ok(BaseResponse.success(singers));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<PagedResponse<SingerDto>>> getAllSingers(
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search) {

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
    public ResponseEntity<BaseResponse<SingerDetailDto>> getSingerById(@PathVariable Long id) {
        SingerDetailDto singer = singerService.getSingerDetailById(id);
        return ResponseEntity.ok(BaseResponse.success(singer));
    }
}