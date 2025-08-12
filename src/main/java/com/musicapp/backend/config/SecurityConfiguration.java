// src/main/java/com/musicapp/backend/config/SecurityConfiguration.java
package com.musicapp.backend.config;

import com.musicapp.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        // == PHẦN 1: CÁC ENDPOINT CÔNG KHAI TUYỆT ĐỐI (cho mọi method) ==
                        .requestMatchers(
                                "/uploads/**",                     // Truy cập file tĩnh
                                "/api/v1/auth/**",                 // Đăng ký, đăng nhập
                                "/v3/api-docs/**",                 // Swagger UI
                                "/swagger-ui/**",                  // Swagger UI
                                "/api/v1/transactions/momo-ipn"    // MoMo IPN Callback
                        ).permitAll()
                        // Cho phép preflight request của CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // == PHẦN 2: CÁC ENDPOINT CÔNG KHAI CHỈ DÀNH CHO METHOD GET ==
                        // Cho phép người dùng chưa đăng nhập có thể xem thông tin
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/songs/**",          // Xem danh sách và chi tiết bài hát
                                "/api/v1/playlists/**",      // Xem danh sách và chi tiết playlist công khai
                                "/api/v1/singers/**",        // Xem danh sách và chi tiết ca sĩ
                                "/api/v1/tags/**",           // Xem danh sách và chi tiết thẻ
                                "/api/v1/likes/*/*/count"    // Xem số lượt like của cả bài hát và playlist
                        ).permitAll()

                        // == PHẦN 3: CÁC ENDPOINT CÒN LẠI YÊU CẦU XÁC THỰC ==
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://127.0.0.1:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}