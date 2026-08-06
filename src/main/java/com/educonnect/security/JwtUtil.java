package com.educonnect.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 🔒 SECURITY FIX: the previous version fell back to a hardcoded secret
 * ("gizliAnahtarEduConnect...") if jwt.secret was missing from config.
 * That string was sitting in source control, so anyone with repo access
 * could forge a valid token for ANY user (including super admin) in any
 * environment that forgot to override the property.
 *
 * Now: jwt.secret is REQUIRED. The app refuses to start without it, and
 * refuses to start if it's too short for HS256. Fail fast beats fail open.
 *
 * Configure it via environment variable, e.g.:
 *   export JWT_SECRET="<a long random value, 32+ bytes>"
 *   application.properties: jwt.secret=${JWT_SECRET}
 */
@Component
public class JwtUtil {

    private static final int MIN_SECRET_BYTES = 32; // HS256 requires >= 256 bits

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;
    private final long expirationTimeMillis = 1000L * 60 * 60 * 10; // 10 hours

    @PostConstruct
    public void initKey() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException(
                    "jwt.secret is not configured. Set the JWT_SECRET environment variable " +
                            "(or jwt.secret property) before starting the application.");
        }
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret is too short (" + keyBytes.length + " bytes). " +
                            "It must be at least " + MIN_SECRET_BYTES + " bytes for HS256.");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserDetails userDetails) {
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("");
        return generateToken(userDetails.getUsername(), role);
    }

    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTimeMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}