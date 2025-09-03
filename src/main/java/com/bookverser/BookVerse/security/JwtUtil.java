package com.bookverser.BookVerse.security;

//public class JwtUtil {
//	  private final String SECRET = "quickbite_jwt_secret_key_which_should_be_long_enough_for_security";
//    private final long EXPIRATION = 1000 * 60 * 60 * 10; // 10 hours
//
//    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
//
//    public String generateToken(String username) {
//        return Jwts.builder()
//                .setSubject(username)
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
//                .signWith(key, SignatureAlgorithm.HS512)
//                .compact();
//    }
//
//    public String extractUsername(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(key)
//                .build()
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject();
//    }
//
//    public boolean validateToken(String token) {
//        try {
//            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
//            return true;
//        } catch (JwtException | IllegalArgumentException e) {
//            return false;
//        }
//    }
//}

//new code
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private final String secret;
    private final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60; // 1 hour
    private final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7 days
    private final Key key;

    public JwtUtil(@Value("${jwt.secret:quickbite_jwt_secret_key_which_is_definitely_long_enough_for_security_purposes_1234567890}") String secret) {
        this.secret = secret;
        logger.info("Using secret key (first 10 chars): {} (length: {} bytes)", secret.substring(0, Math.min(10, secret.length())), secret.getBytes(StandardCharsets.UTF_8).length);
        try {
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < 64) {
                logger.error("Secret key is too short for HS512. It must be at least 64 bytes. Current length: {}", keyBytes.length);
                throw new IllegalArgumentException("Secret key is too short for HS512.");
            }
            this.key = Keys.hmacShaKeyFor(keyBytes);
            logger.info("Key initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize key: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize JWT key", e);
        }
    }

    @PostConstruct
    public void init() {
        logger.info("JwtUtil initialized with key: {}", key != null ? "present" : "null");
    }

    public String generateToken(String username) {
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuer("quickbite")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
        logger.debug("Generated token for user {}: {}", username, token);
        return token;
    }

    public String generateRefreshToken(String username) {
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuer("quickbite")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
        logger.debug("Generated refresh token for user {}: {}", username, token);
        return token;
    }

    public String extractUsername(String token) {
        try {
            logger.debug("Attempting to extract username from token: {}", token);
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .setAllowedClockSkewSeconds(300)
                    .requireIssuer("quickbite")
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e) {
            logger.error("Failed to extract username from JWT: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            logger.debug("Validating token: {}", token);
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .setAllowedClockSkewSeconds(300)
                    .requireIssuer("quickbite")
                    .build()
                    .parseClaimsJws(token);
            logger.debug("Token validated successfully");
            return true;
        } catch (ExpiredJwtException e) {
            logger.error("JWT expired: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }


}
