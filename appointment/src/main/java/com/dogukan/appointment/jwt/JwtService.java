package com.dogukan.appointment.jwt;

import com.dogukan.appointment.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${JWT_SECRET_KEY}")
    private String jwtSecret;

    @Value("${JWT_EXPIRATION}")
    private long expiration; // ms cinsinden (Örn: 15 dakika = 900000 ms)


    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .claim("role",user.getRole().name())
                .claim("userId", user.getUserId().toString()) // <-- EKLENDİ: ID'yi token'a eklemek yetki kontrolünü hızlandırır.
                .setExpiration(new Date(System.currentTimeMillis() +  expiration)) // <-- DÜZELTME: Yapılandırılan süreyi kullan
                .signWith(getKey(),SignatureAlgorithm.HS256)
                .compact();
    }

    public <T> T exportToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token).getBody();
    }

    public String extractUserEmail(String token) {
        return getClaims(token).getSubject();
    }

    public String getUsernameByToken(String token) {
        return exportToken(token,Claims::getSubject);
    }

    public boolean isTokenValid(String token) {
        Date expireDate = exportToken(token,Claims::getExpiration);
        return new Date().before(expireDate);
    }

    public Key getKey() {
        byte[] bytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(bytes);
    }
}