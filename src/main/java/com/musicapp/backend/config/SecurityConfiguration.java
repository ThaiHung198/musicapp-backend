package com.musicapp.backend.config;

import com.musicapp.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    private static final String[] WHITE_LIST_URL = {
            "/api/v1/auth/**",
            "/api/v1/songs",          // Public song listing
            "/api/v1/songs/{id}",     // Public song details  
            "/api/v1/songs/top",      // Public top songs
            "/api/v1/songs/recent",   // Public recent songs
            "/api/v1/songs/most-liked", // Public most liked songs
            "/api/v1/songs/singer/**", // Public songs by singer
            "/api/v1/songs/{id}/listen", // Public listen count increment
            "/api/v1/singers",        // Public singer listing
            "/api/v1/singers/{id}",   // Public singer details
            "/api/v1/singers/list",   // Public singer list
            "/api/v1/tags",           // Public tag listing
            "/api/v1/tags/{id}",      // Public tag details
            "/api/v1/likes/songs/{id}/count",     // Public like counts
            "/api/v1/likes/playlists/{id}/count", // Public like counts
            "/v3/api-docs/**",        // Swagger
            "/swagger-ui/**"          // Swagger
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req ->
                        req.requestMatchers(WHITE_LIST_URL)
                                .permitAll()
                                .anyRequest()
                                .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}