package com.musicapp.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.playlist.CreatePlaylistRequest;
import com.musicapp.backend.dto.playlist.PlaylistDto;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/v1/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<PlaylistDto>> createPlaylist(
            @RequestPart("request") String requestJson,
            @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            @AuthenticationPrincipal User currentUser
    ) throws JsonProcessingException {
        CreatePlaylistRequest request = new ObjectMapper().readValue(requestJson, CreatePlaylistRequest.class);
        PlaylistDto newPlaylist = playlistService.createPlaylist(request, thumbnailFile, currentUser);
        return ResponseEntity.ok(BaseResponse.success("Tạo playlist thành công!", newPlaylist));
    }

    @GetMapping("/my-playlists")
    public ResponseEntity<BaseResponse<List<PlaylistDto>>> getMyPlaylists(
            @AuthenticationPrincipal User currentUser
    ) {
        List<PlaylistDto> myPlaylists = playlistService.getMyPlaylists(currentUser);
        return ResponseEntity.ok(BaseResponse.success("Lấy danh sách playlist thành công!", myPlaylists));
    }

}