package com.itdev.finalproject.service;

import com.itdev.finalproject.dto.SignInDto;
import com.itdev.finalproject.security.JwtTokenManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class JwtAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenManager jwtTokenManager;

    public JwtAuthenticationService(AuthenticationManager authenticationManager,
                                    JwtTokenManager jwtTokenManager) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenManager = jwtTokenManager;
    }

    public String authenticateUser(SignInDto user) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                user.username(),
                user.password()));
        return jwtTokenManager.generateToken(user.username());
    }
}
