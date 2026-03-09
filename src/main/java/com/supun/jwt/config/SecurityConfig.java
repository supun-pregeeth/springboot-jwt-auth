package com.supun.jwt.config;

import com.supun.jwt.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration //This class contains configuration settings for the application
public class SecurityConfig {

    //this is the guard
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    //this defines the security pipeline for every request.
    //Request → SecurityFilterChain → Controller
    public SecurityFilterChain securityFilterChain(
            @org.jetbrains.annotations.NotNull HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())//Disable CSRF- it is used mainly for web. But when using JWT APIs, we usually disable it.
                //This defines who can enter which room.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();//Okay, the security rules are ready. Activate them.
    }
}