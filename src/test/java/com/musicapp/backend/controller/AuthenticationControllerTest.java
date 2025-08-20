package com.musicapp.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.musicapp.backend.config.SecurityConfiguration;
import com.musicapp.backend.dto.AuthenticationResponse;
import com.musicapp.backend.dto.RegisterRequest;
import com.musicapp.backend.exception.ResourceAlreadyExistsException;
import com.musicapp.backend.security.JwtService;
import com.musicapp.backend.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Bước 1: Chỉ định controller cần test
@WebMvcTest(AuthenticationController.class)
// Bước 2: Import tường minh cấu hình bảo mật thật
@Import(SecurityConfiguration.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Bước 3: Mock tất cả các dependency mà controller và SecurityConfiguration cần
    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    // UserDetailsService được AuthenticationProvider cần, nên không cần mock trực tiếp nếu đã mock AuthenticationProvider

    private RegisterRequest validRegisterRequest;
    private AuthenticationResponse authenticationResponse;

    @BeforeEach
    void setUp() {
        validRegisterRequest = RegisterRequest.builder()
                .displayName("Test User")
                .email("test@example.com")
                .phoneNumber("0987654321")
                .password("password123")
                .build();

        authenticationResponse = AuthenticationResponse.builder()
                .token("dummy-jwt-token")
                .build();
    }

    @Test
    void testRegister_Success() throws Exception {
        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(authenticationResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đăng ký tài khoản thành công!"))
                .andExpect(jsonPath("$.data.token").value("dummy-jwt-token"));
    }

    @Test
    void testRegister_InvalidRequest_ReturnsBadRequest() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .displayName("Test User")
                .email("")
                .phoneNumber("0987654321")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_EmailExists_ReturnsConflict() throws Exception {
        String errorMessage = "Email đã tồn tại.";
        when(authenticationService.register(any(RegisterRequest.class)))
                .thenThrow(new ResourceAlreadyExistsException(errorMessage));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }
}