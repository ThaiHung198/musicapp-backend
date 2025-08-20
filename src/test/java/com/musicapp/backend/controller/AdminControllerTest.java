package com.musicapp.backend.controller;

import com.musicapp.backend.config.SecurityConfiguration;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.creator.CreatorViewDto;
import com.musicapp.backend.security.JwtService;
import com.musicapp.backend.service.UserService;
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

@WebMvcTest(AdminController.class)
@Import(SecurityConfiguration.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService; // AdminController cần UserService

    // Các mock cho SecurityConfiguration
    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllCreators_AsAdmin_Success() throws Exception {
        // --- Arrange (Given) ---
        // Chuẩn bị dữ liệu trả về mẫu
        CreatorViewDto creatorDto = new CreatorViewDto();
        creatorDto.setId(1L);
        creatorDto.setDisplayName("Creator Name");
        creatorDto.setEmail("creator@example.com");
        List<CreatorViewDto> creatorList = List.of(creatorDto);

        // 1. Tạo một đối tượng Page hoàn chỉnh
        PageRequest pageRequest = PageRequest.of(0, 5);
        Page<CreatorViewDto> creatorPage = new PageImpl<>(creatorList, pageRequest, 1);

        // 2. Dùng phương thức PagedResponse.of() để tạo đối tượng
        PagedResponse<CreatorViewDto> pagedResponse = PagedResponse.of(creatorPage.getContent(), creatorPage);

        // Dạy cho mock service trả về đối tượng đã tạo đúng cách
        when(userService.getAllCreators(any(), any())).thenReturn(pagedResponse);

        // --- Act & Assert (When & Then) ---
        mockMvc.perform(get("/api/v1/admin/creators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].displayName").value("Creator Name"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetAllCreators_AsUser_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/creators"))
                .andExpect(status().isForbidden()); // Mong đợi HTTP 403 Forbidden
    }
}