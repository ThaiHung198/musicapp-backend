package com.musicapp.backend.config;

import com.musicapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    /**
     * Bean này chỉ cho Spring Security cách để tìm một người dùng.
     * Khi AuthenticationManager cần tìm user, nó sẽ gọi đến bean này.
     * Ta chỉ định nó tìm bằng email thông qua method reference `userRepository::findByEmail`.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    /**
     * Bean này là "trái tim" của việc xác thực. Nó kết hợp 2 thứ:
     * 1. Cách tìm người dùng (userDetailsService).
     * 2. Cách kiểm tra mật khẩu (passwordEncoder).
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Bean này quản lý toàn bộ quá trình xác thực và sử dụng AuthenticationProvider đã được cấu hình ở trên.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Bean này định nghĩa thuật toán mã hóa mật khẩu.
     * Luôn phải là BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}