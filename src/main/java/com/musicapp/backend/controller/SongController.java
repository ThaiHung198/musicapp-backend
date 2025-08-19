package com.musicapp.backend.controller;

import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.ChartSongDto;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.song.*;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @GetMapping("/all-for-playlist")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<List<SongDto>>> getAllSongsForPlaylist(@AuthenticationPrincipal User currentUser) {
        List<SongDto> songs = songService.getAllSongsForPlaylist(currentUser);
        return ResponseEntity.ok(BaseResponse.success(songs));
    }

    @GetMapping("/playlist/{playlistId}/search")
    public ResponseEntity<BaseResponse<List<SongDto>>> searchForPlaylist(
            @PathVariable Long playlistId,
            @RequestParam(defaultValue = "") String keyword,
            @AuthenticationPrincipal User currentUser) {
        List<SongDto> songs = songService.searchApprovedSongsForPlaylist(playlistId, keyword, currentUser);
        return ResponseEntity.ok(BaseResponse.success("Tìm kiếm bài hát trong playlist thành công.", songs));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<PagedResponse<SongDto>>> getAllSongsForAdmin(
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal User admin) {

        Page<SongDto> songs = songService.getAllSongsForAdmin(search, pageable, admin);
        PagedResponse<SongDto> response = PagedResponse.of(songs.getContent(), songs);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<PagedResponse<SongDto>>> getAllSongs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
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

    @PostMapping("/{id}/toggle-visibility")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<SongDto>> toggleVisibility(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        SongDto updatedSong = songService.toggleSongVisibility(id, admin);
        String message = updatedSong.getStatus().equals("HIDDEN") ? "Ẩn bài hát thành công." : "Hiện bài hát thành công.";
        return ResponseEntity.ok(BaseResponse.success(message, updatedSong));
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
            @RequestParam(defaultValue = "5") int size,
            @AuthenticationPrincipal User currentUser) {

        Pageable pageable = PageRequest.of(page, size);
        Page<SongDto> songs = songService.getSongsBySinger(singerId, pageable, currentUser);
        PagedResponse<SongDto> response = PagedResponse.of(songs.getContent(), songs);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PostMapping(value = "/admin", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<SongDto>> createSongByAdmin(
            @RequestPart("songRequest") @Valid AdminCreateSongRequest request,
            @RequestPart("audioFile") MultipartFile audioFile,
            @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            @RequestPart(value = "newSingerAvatars", required = false) List<MultipartFile> newSingerAvatars,
            @AuthenticationPrincipal User admin) {
        SongDto newSong = songService.createSongByAdmin(request, audioFile, thumbnailFile, newSingerAvatars, admin);
        return ResponseEntity.ok(BaseResponse.success("Song created and approved successfully", newSong));
    }

    @PutMapping(value = "/admin/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<SongDto>> updateSongByAdmin(
            @PathVariable Long id,
            @RequestPart("songRequest") @Valid AdminUpdateSongRequest request,
            @RequestPart(value = "audioFile", required = false) MultipartFile audioFile,
            @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            @AuthenticationPrincipal User admin) {
        SongDto updatedSong = songService.updateSongByAdmin(id, request, audioFile, thumbnailFile, admin);
        return ResponseEntity.ok(BaseResponse.success("Song updated successfully", updatedSong));
    }

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
            @RequestParam(defaultValue = "5") int size,
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

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<PagedResponse<SongDto>>> getPendingSongs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

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

    @GetMapping("/my-library")
    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<PagedResponse<SongDto>>> getMyLibrary(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            Authentication authentication) {
        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PagedResponse<SongDto> response = songService.getMyLibrary(username, name, pageable);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/{songId}/lyrics")
    public ResponseEntity<BaseResponse<List<LyricLineDto>>> getSongLyrics(@PathVariable Long songId) {
        List<LyricLineDto> lyrics = songService.getParsedLyrics(songId);
        return ResponseEntity.ok(new BaseResponse<>(true, "Lyrics fetched successfully", lyrics));
    }

}