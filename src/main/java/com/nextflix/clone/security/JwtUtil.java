// java
package com.nextflix.clone.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    private static final long JWT_TOKEN_VALIDITY = 30L * 24 * 60 * 60 * 1000; // 30 days in milliseconds

    @Value("${jwt.secret:defaultSecretKeyForNextflixClone}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String getRoleFromToken(String token) {
        return getAllClaimsFromToken(token, claims -> claims.get("role", String.class));
    }

    public String getUsernameFromToken(String token) {
        return getAllClaimsFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getAllClaimsFromToken(token, Claims::getExpiration);
    }

    private <T> T getAllClaimsFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseEncryptedClaims(token).getPayload();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return doGenerateToken(claims, username);
    }

    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims) // Nhận vào một Map chứa các claim tùy chỉnh, trong đó "role" được đặt là một claim
                .subject(subject) // Đặt subject của token, thường là username hoặc user ID
                .issuedAt(new Date(System.currentTimeMillis())) // Đặt thời gian phát hành của token là thời điểm hiện tại
                .expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY)) // Đặt thời gian hết hạn của token là 30 ngày kể từ thời điểm hiện tại
                .signWith(getSigningKey()) // Ký token bằng secret key đã được cấu hình
                .compact(); // Trả về token đã được tạo dưới dạng chuỗi
    }

    public Boolean validateToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}