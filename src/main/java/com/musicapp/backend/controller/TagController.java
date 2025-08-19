// File: src/main/java/com/musicapp/backend/controller/TagController.java
package com.musicapp.backend.controller;

import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.tag.AdminCreateMultipleTagsRequest;
import com.musicapp.backend.dto.tag.CreateTagRequest;
import com.musicapp.backend.dto.tag.TagAdminViewDto;
import com.musicapp.backend.dto.tag.TagDto;
import com.musicapp.backend.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<TagDto>>> getAllTags() {
        List<TagDto> tags = tagService.getAllTags();
        return ResponseEntity.ok(BaseResponse.success(tags));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<PagedResponse<TagAdminViewDto>>> getAllTagsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        PagedResponse<TagAdminViewDto> pagedTags = tagService.getAllTagsForAdmin(search, pageable);
        return ResponseEntity.ok(BaseResponse.success(pagedTags));
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<TagDto>> createTag(@Valid @RequestBody CreateTagRequest request) {
        TagDto tag = tagService.createTag(request);
        return ResponseEntity.ok(BaseResponse.success("Tạo tag mới thành công.", tag));
    }

    // START-CHANGE: Thêm endpoint mới để tạo nhiều tags
    @PostMapping("/admin/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<List<TagDto>>> createMultipleTags(@Valid @RequestBody AdminCreateMultipleTagsRequest request) {
        List<TagDto> tags = tagService.createMultipleTags(request);
        return ResponseEntity.ok(BaseResponse.success("Tạo " + tags.size() + " tags mới thành công.", tags));
    }
    // END-CHANGE

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<TagDto>> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody CreateTagRequest request) {
        TagDto tag = tagService.updateTag(id, request);
        return ResponseEntity.ok(BaseResponse.success("Cập nhật tag thành công.", tag));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok(BaseResponse.success("Xóa tag thành công.", null));
    }
}