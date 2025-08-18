package com.musicapp.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicapp.backend.config.SecurityConfiguration;
import com.musicapp.backend.dto.playlist.CreatePlaylistRequest;
import com.musicapp.backend.dto.playlist.PlaylistDto;
import com.musicapp.backend.security.JwtService;
import com.musicapp.backend.service.PlaylistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlaylistController.class)
@Import(SecurityConfiguration.class)
class PlaylistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlaylistService playlistService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthenticationProvider authenticationProvider;


    @Test
    @WithMockUser(username = "test@user.com", roles = "USER")
    void testCreatePlaylist_Success() throws Exception {
        // --- Arrange (Given) ---
        // 1. Chuẩn bị request DTO và file ảnh giả
        CreatePlaylistRequest createRequest = new CreatePlaylistRequest("My New Playlist", List.of(1L, 2L));
        MockMultipartFile thumbnailFile = new MockMultipartFile(
                "thumbnailFile", // Tên của @RequestPart trong controller
                "thumbnail.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "some-image-bytes".getBytes()
        );

        // 2. Vì @RequestPart("playlistRequest") là JSON, ta phải tạo một MockMultipartFile riêng cho nó
        MockMultipartFile playlistRequestJson = new MockMultipartFile(
                "playlistRequest", // Tên của @RequestPart trong controller
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(createRequest).getBytes()
        );

        // 3. Chuẩn bị DTO kết quả mà service sẽ trả về
        PlaylistDto expectedResult = PlaylistDto.builder()
                .id(100L)
                .name("My New Playlist")
                .creatorName("test@user.com")
                .songCount(2)
                .build();

        // 4. Dạy cho mock service
        when(playlistService.createPlaylist(any(CreatePlaylistRequest.class), any(MultipartFile.class), any()))
                .thenReturn(expectedResult);

        // --- Act & Assert (When & Then) ---
        mockMvc.perform(multipart("/api/v1/playlists") // Sử dụng multipart() thay vì post()
                        .file(playlistRequestJson) // Thêm phần JSON
                        .file(thumbnailFile))      // Thêm phần file ảnh
                .andExpect(status().isCreated()) // Mong đợi HTTP 201 Created
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100L))
                .andExpect(jsonPath("$.data.name").value("My New Playlist"))
                .andExpect(jsonPath("$.data.songCount").value(2));
    }
}