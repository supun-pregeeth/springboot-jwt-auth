package com.supun.jwt.service;

import com.supun.jwt.dto.*;
import com.supun.jwt.entity.User;
import com.supun.jwt.repo.UserRepository;
import com.supun.jwt.security.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository,
                       JwtService jwtService){

        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public AuthResponse signup(SignupRequest request){

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        String token = jwtService.generateToken(user.getUsername());

        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request){

        User user = userRepository.findByEmail(request.getEmail()
                )
                .orElseThrow();

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(user.getUsername());

        return new AuthResponse(token);
    }
}