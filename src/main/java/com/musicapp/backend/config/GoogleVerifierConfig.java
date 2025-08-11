package com.musicapp.backend.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.util.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class GoogleVerifierConfig {

    @Value("${application.security.oauth2.google.client-id}")
    private String googleClientId;

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier() {
        return new GoogleIdTokenVerifier.Builder(Utils.getDefaultTransport(), Utils.getDefaultJsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }
}
