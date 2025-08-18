package com.musicapp.backend.controller;

import com.musicapp.backend.config.SecurityConfiguration;
import com.musicapp.backend.dto.song.SongDto;
import com.musicapp.backend.security.JwtService;
import com.musicapp.backend.service.SongService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SongController.class)
@Import(SecurityConfiguration.class)
class SongControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SongService songService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @Test
    void testGetAllSongs_Success() throws Exception {
        // --- Arrange (Given) ---
        // 1. Chuẩn bị dữ liệu mẫu
        SongDto song1 = SongDto.builder().id(1L).title("Song Title 1").creatorName("Creator 1").build();
        SongDto song2 = SongDto.builder().id(2L).title("Song Title 2").creatorName("Creator 2").build();
        List<SongDto> songList = List.of(song1, song2);

        // 2. Tạo một đối tượng Page<SongDto> để giả lập kết quả trả về từ service
        PageRequest pageRequest = PageRequest.of(0, 5);
        Page<SongDto> songPage = new PageImpl<>(songList, pageRequest, songList.size());

        // 3. Dạy cho mock service: khi được gọi, hãy trả về đối tượng Page<SongDto> đã chuẩn bị
        when(songService.getAllApprovedSongs(any(), any())).thenReturn(songPage);

        // --- Act & Assert (When & Then) ---
        mockMvc.perform(get("/api/v1/songs")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.pageInfo.page").value(0))
                .andExpect(jsonPath("$.data.content[0].title").value("Song Title 1"))
                .andExpect(jsonPath("$.data.content[0].creatorName").value("Creator 1"));
    }

    @Test
    @WithMockUser(username = "premium@user.com", roles = "USER") // Giả lập user đã đăng nhập
    void testGetAllSongs_AsPremiumUser() throws Exception {
        // --- Arrange (Given) ---
        // 1. Tạo một bài hát Premium. Quan trọng là ta giả lập service trả về `canAccess = true`
        SongDto premiumSong = SongDto.builder()
                .id(1L)
                .title("Premium Song")
                .isPremium(true)
                .canAccess(true)
                .build();
        List<SongDto> songList = List.of(premiumSong);

        // 2. Chuẩn bị dữ liệu phân trang
        Page<SongDto> songPage = new PageImpl<>(songList, PageRequest.of(0, 5), 1);

        // 3. Dạy cho mock service
        when(songService.getAllApprovedSongs(any(), any())).thenReturn(songPage);

        // --- Act & Assert (When & Then) ---
        mockMvc.perform(get("/api/v1/songs")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("Premium Song"))
                // Kiểm tra các trường quan trọng
                .andExpect(jsonPath("$.data.content[0].isPremium").value(true))
                .andExpect(jsonPath("$.data.content[0].canAccess").value(true));
    }
}