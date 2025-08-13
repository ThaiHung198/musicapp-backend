package com.musicapp.backend.controller;

import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.creator.CreatorDetailDto;
import com.musicapp.backend.dto.creator.CreatorViewDto;
import com.musicapp.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @GetMapping("/creators")
    public ResponseEntity<BaseResponse<PagedResponse<CreatorViewDto>>> getAllCreators(
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search) {
        PagedResponse<CreatorViewDto> response = userService.getAllCreators(search, pageable);
        return ResponseEntity.ok(BaseResponse.success("Lấy danh sách Creator thành công.", response));
    }

    @GetMapping("/creators/{creatorId}")
    public ResponseEntity<BaseResponse<CreatorDetailDto>> getCreatorDetails(@PathVariable Long creatorId) {
        CreatorDetailDto response = userService.getCreatorDetails(creatorId);
        return ResponseEntity.ok(BaseResponse.success("Lấy thông tin chi tiết Creator thành công.", response));
    }
}