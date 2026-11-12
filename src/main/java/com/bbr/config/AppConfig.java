package com.bbr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableAsync   // lets @Async event listeners run in the background
public class AppConfig {

    /** HTTP client for the Banana API. The timeout stops the game from hanging if the API is slow. */
    @Bean
    public RestClient bananaRestClient(@Value("${app.banana.timeout-seconds:5}") int seconds) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(seconds));
        factory.setReadTimeout(Duration.ofSeconds(seconds));
        return RestClient.builder().requestFactory(factory).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {    // hashes passwords and OTP codes
        return new BCryptPasswordEncoder();
    }
}
