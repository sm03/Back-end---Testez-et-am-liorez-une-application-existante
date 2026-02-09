package com.openclassrooms.etudiant.configuration.security;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    // 🔐 Use at least 256-bit secret for HS256
    private static final String SECRET_KEY =
            "my-super-secret-open-classroom-project-#2-key-which-needs-to-be-long-enough-okay?";

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    private static final long EXPIRATION_MS = 1000 * 60 * 60; // 1 heure

    public static long getExpirationMs() {
        return EXPIRATION_MS;
    }

    public static long getExpirationSeconds() {
        return EXPIRATION_MS / 1000; // Convertir en secondes
    }

    public String generateToken(UserDetails userDetails) {      
        return io.jsonwebtoken.Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Méthode ajoutée pour le CRUD sécurisé étudiant
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Méthode ajoutée pour le CRUD sécurisé étudiant
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Méthode ajoutée pour le CRUD sécurisé étudiant
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername());
    }

}
