package com.itdev.finalproject.http.rest;

import com.itdev.finalproject.dto.JwtRequest;
import com.itdev.finalproject.dto.JwtResponse;
import com.itdev.finalproject.dto.SignInDto;
import com.itdev.finalproject.service.JwtAuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
public class AuthRestController {

    private final JwtAuthenticationService jwtService;

    public AuthRestController(JwtAuthenticationService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping()
    public ResponseEntity<JwtResponse> authentication(
            @RequestBody @Valid SignInDto user) {
        var token = jwtService.authenticateUser(user);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestBody() JwtRequest token) {
        if (jwtService.validateJwtToken(token.jwt())) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }
    }
}
