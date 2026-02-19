package com.itdev.finalproject.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itdev.finalproject.dto.AuthenticatedUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenManager {

    private final SecretKey key;
    private final long expirationTime;
    private final ObjectMapper objectMapper;

    public JwtTokenManager(@Value("${jwt.secret-key}") String key,
                           @Value("${jwt.lifetime}") long expirationTime,
                           ObjectMapper objectMapper) {
        this.key = Keys.hmacShaKeyFor(key.getBytes());
        this.expirationTime = expirationTime;
        this.objectMapper = objectMapper;
    }

    public String generateToken(AuthenticatedUser user) {
        String subject;
        try {
            subject = objectMapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return Jwts.builder()
                .subject(subject)
                .signWith(key)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .compact();
    }

    public AuthenticatedUser getAuthenticatedUserFromToken(String jwt) {
        String subject = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .getPayload()
                .getSubject();

        AuthenticatedUser user;
        try {
            user = objectMapper.readValue(subject, AuthenticatedUser.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return user;
    }
}
