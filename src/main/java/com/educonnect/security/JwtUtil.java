package com.educonnect.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // 1. GİZLİ MÜHÜRÜMÜZ: Korsanların sahte bilet basmasını engelleyen en az 256-bit uzunluğunda şifre
    private static final String SECRET_KEY = "EduConnectProjesininCokGizliVeGuvenliAnahtariBuKadarUzunOlmali";

    // Şifreyi JJWT kütüphanesinin anlayacağı formata çeviriyoruz
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // 2. BİLETİN ÖMRÜ: 10 Saat (Milisaniye cinsinden)
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 10;

    // --- BİLET (TOKEN) BASMA İŞLEMİ ---
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) // Biletin sahibi (Kullanıcı adı)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Basım zamanı
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Bitiş zamanı
                .signWith(key, SignatureAlgorithm.HS256) // Kurşun geçirmez mührümüzü basıyoruz
                .compact();
    }

    // --- BİLET OKUMA VE DOĞRULAMA İŞLEMLERİ ---

    // Biletin üstünde yazan ismi (Kullanıcı Adını) okur
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Biletin süresi dolmuş mu diye bakar
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Gelen biletin hem doğru kişiye ait olup olmadığını hem de süresini kontrol eder
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Yardımcı Metotlar (Biletin içindeki kriptolu verileri çözmek için)
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
