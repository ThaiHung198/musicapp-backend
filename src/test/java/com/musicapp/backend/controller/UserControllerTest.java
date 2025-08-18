package com.musicapp.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicapp.backend.config.SecurityConfiguration;
import com.musicapp.backend.dto.user.UserProfileDto;
import com.musicapp.backend.security.JwtService;
import com.musicapp.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser; // <-- IMPORT QUAN TRỌNG
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfiguration.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // UserController phụ thuộc vào UserService, nên ta cần mock nó
    @MockBean
    private UserService userService;

    // Các mock này là để SecurityConfiguration có thể được load
    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testGetCurrentUserProfile_Success() throws Exception {
        // --- Arrange (Given) ---
        // Chuẩn bị dữ liệu mẫu mà service sẽ trả về
        UserProfileDto expectedProfile = UserProfileDto.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();

        // Dạy cho mock service: khi phương thức getCurrentUserProfile được gọi với bất kỳ User nào,
        // hãy trả về đối tượng expectedProfile
        when(userService.getCurrentUserProfile(any())).thenReturn(expectedProfile);

        // --- Act & Assert (When & Then) ---
        mockMvc.perform(get("/api/v1/users/me")) // Thực hiện request GET
                .andExpect(status().isOk()) // Mong đợi HTTP 200 OK
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com")) // Kiểm tra dữ liệu trả về
                .andExpect(jsonPath("$.data.displayName").value("Test User"));
    }
}