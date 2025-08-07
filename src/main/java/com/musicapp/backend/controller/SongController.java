package com.musicapp.backend.controller;

import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.song.CreateSongRequest;
import com.musicapp.backend.dto.song.SongDto;
import com.musicapp.backend.dto.song.UpdateSongRequest;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.service.SongService;
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
@RequestMapping("/api/v1/songs")
@RequiredArgsConstructor
public class SongController {
    
    private final SongService songService;
    
    // Public endpoints (accessible by guests)
    @GetMapping
    public ResponseEntity<BaseResponse<PagedResponse<SongDto>>> getAllSongs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal User currentUser) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<SongDto> songs;
        
        if (search != null && !search.trim().isEmpty()) {
            songs = songService.searchSongs(search.trim(), pageable, currentUser);
        } else {
            songs = songService.getAllApprovedSongs(pageable, currentUser);
        }
        
        PagedResponse<SongDto> response = PagedResponse.of(songs.getContent(), songs);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<SongDto>> getSongById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        SongDto song = songService.getSongById(id, currentUser);
        return ResponseEntity.ok(BaseResponse.success(song));
    }
    
    @PostMapping("/{id}/listen")
    public ResponseEntity<BaseResponse<Void>> incrementListenCount(@PathVariable Long id) {
        songService.incrementListenCount(id);
        return ResponseEntity.ok(BaseResponse.success("Listen count incremented", null));
    }
    
    @GetMapping("/top")
    public ResponseEntity<BaseResponse<List<SongDto>>> getTopSongs(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal User currentUser) {
        List<SongDto> songs = songService.getTopSongsByListenCount(limit, currentUser);
        return ResponseEntity.ok(BaseResponse.success(songs));
    }
    
    @GetMapping("/recent")
    public ResponseEntity<BaseResponse<List<SongDto>>> getRecentSongs(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal User currentUser) {
        List<SongDto> songs = songService.getRecentlyCreatedSongs(limit, currentUser);
        return ResponseEntity.ok(BaseResponse.success(songs));
    }
    
    @GetMapping("/most-liked")
    public ResponseEntity<BaseResponse<List<SongDto>>> getMostLikedSongs(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal User currentUser) {
        List<SongDto> songs = songService.getMostLikedSongs(limit, currentUser);
        return ResponseEntity.ok(BaseResponse.success(songs));
    }
    
    @GetMapping("/singer/{singerId}")
    public ResponseEntity<BaseResponse<PagedResponse<SongDto>>> getSongsBySinger(
            @PathVariable Long singerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<SongDto> songs = songService.getSongsBySinger(singerId, pageable, currentUser);
        PagedResponse<SongDto> response = PagedResponse.of(songs.getContent(), songs);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
    
    // Creator endpoints
    @PostMapping
    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<SongDto>> createSong(
            @Valid @RequestBody CreateSongRequest request,
            @AuthenticationPrincipal User creator) {
        SongDto song = songService.createSong(request, creator);
        return ResponseEntity.ok(BaseResponse.success("Song created successfully", song));
    }
    
    @GetMapping("/my-songs")
    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<PagedResponse<SongDto>>> getMySongs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User creator) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<SongDto> songs = songService.getUserCreatedSongs(creator.getId(), pageable, creator);
        PagedResponse<SongDto> response = PagedResponse.of(songs.getContent(), songs);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
    
    @GetMapping("/{id}/creator")
    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<SongDto>> getSongByIdForCreator(
            @PathVariable Long id,
            @AuthenticationPrincipal User creator) {
        SongDto song = songService.getSongByIdForCreator(id, creator);
        return ResponseEntity.ok(BaseResponse.success(song));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<SongDto>> updateSong(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSongRequest request,
            @AuthenticationPrincipal User user) {
        SongDto song = songService.updateSong(id, request, user);
        return ResponseEntity.ok(BaseResponse.success("Song updated successfully", song));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteSong(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        songService.deleteSong(id, user);
        return ResponseEntity.ok(BaseResponse.success("Song deleted successfully", null));
    }
    
    // Admin endpoints
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<PagedResponse<SongDto>>> getPendingSongs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<SongDto> songs = songService.getPendingSongs(pageable);
        PagedResponse<SongDto> response = PagedResponse.of(songs.getContent(), songs);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
    
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<SongDto>> approveSong(@PathVariable Long id) {
        SongDto song = songService.approveSong(id);
        return ResponseEntity.ok(BaseResponse.success("Song approved successfully", song));
    }
    
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<SongDto>> rejectSong(@PathVariable Long id) {
        SongDto song = songService.rejectSong(id);
        return ResponseEntity.ok(BaseResponse.success("Song rejected successfully", song));
    }

    /**
     * Endpoint cho creator để lấy danh sách các bài hát đã được duyệt của mình (có phân trang).
     */
    @GetMapping("/my/approved")
    @PreAuthorize("hasRole('CREATOR')")
    public ResponseEntity<PagedResponse<SongDto>> getMyApprovedSongs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);

        PagedResponse<SongDto> response = songService.getMyApprovedSongs(username, pageable);

        return ResponseEntity.ok(response);
    }


    /**
     * Endpoint cho creator xem chi tiết bài hát đã được duyệt của mình.
     */
    @GetMapping("/my/{id}")
    @PreAuthorize("hasRole('CREATOR')")
    public ResponseEntity<BaseResponse<SongDto>> getMyApprovedSongDetails(
            @PathVariable Long id,
            Authentication authentication) {

        String username = authentication.getName();
        SongDto songDto = songService.getMyApprovedSongDetails(id, username);
        return ResponseEntity.ok(BaseResponse.success("Approved song details retrieved successfully", songDto));
    }
}
