package com.musicapp.backend.controller;

import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.tag.TagDto;
import com.musicapp.backend.service.TagService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<BaseResponse<List<TagDto>>> getAllTags(
            @RequestParam(required = false) String search) {
        
        List<TagDto> tags;
        if (search != null && !search.trim().isEmpty()) {
            tags = tagService.searchTags(search.trim());
        } else {
            tags = tagService.getAllTags();
        }
        
        return ResponseEntity.ok(BaseResponse.success(tags));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<TagDto>> getTagById(@PathVariable Long id) {
        TagDto tag = tagService.getTagById(id);
        return ResponseEntity.ok(BaseResponse.success(tag));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<TagDto>> createTag(@RequestParam String name) {
        TagDto tag = tagService.createTag(name);
        return ResponseEntity.ok(BaseResponse.success("Tag created successfully", tag));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<TagDto>> updateTag(
            @PathVariable Long id,
            @RequestParam String name) {
        TagDto tag = tagService.updateTag(id, name);
        return ResponseEntity.ok(BaseResponse.success("Tag updated successfully", tag));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok(BaseResponse.success("Tag deleted successfully", null));
    }
}
