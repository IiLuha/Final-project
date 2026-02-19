package com.itdev.finalproject.service;

import com.itdev.finalproject.dto.AuthenticatedUser;
import com.itdev.finalproject.dto.SignInDto;
import com.itdev.finalproject.security.JwtTokenManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JwtAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenManager jwtTokenManager;
    private final UserDetailsService userService;

    public JwtAuthenticationService(AuthenticationManager authenticationManager,
                                    JwtTokenManager jwtTokenManager, UserDetailsService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenManager = jwtTokenManager;
        this.userService = userService;
    }

    public String authenticateUser(SignInDto maybeUser) {
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                maybeUser.username(),
                maybeUser.password()));
        AuthenticatedUser user = (AuthenticatedUser) authenticate.getPrincipal();
        return jwtTokenManager.generateToken(user);
    }

    public boolean validateJwtToken(String token) {
        try {
            loadUserByJwtToken(token);
            return true;
        } catch (UsernameNotFoundException e) {
            return false;
        }
    }

    public UserDetails loadUserByJwtToken(String jwtToken) {
        AuthenticatedUser user = jwtTokenManager.getAuthenticatedUserFromToken(jwtToken);
        return userService.loadUserByUsername(user.getUsername());
    }
}
