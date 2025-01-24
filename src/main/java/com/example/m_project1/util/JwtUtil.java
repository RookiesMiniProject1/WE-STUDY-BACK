package com.example.m_project1.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Slf4j
@Component
public class JwtUtil {
    private static final String AUTHORITIES_KEY = "auth";
    private static final String EMAIL_KEY = "email";
    private static final String TOKEN_TYPE = "type";
    private static final String TOKEN_TYPE_ACCESS = "ACCESS";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationTime;

    @Value("${jwt.issuer}")
    private String issuer;


    private Key SECRET_KEY;

    @PostConstruct
    public void init() {
        this.SECRET_KEY = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(AUTHORITIES_KEY, role);
        claims.put(EMAIL_KEY, email);
        claims.put("tokenId", UUID.randomUUID().toString());
        claims.put("clientHash", generateClientHash(email));
        claims.put("iat", new Date(System.currentTimeMillis()));
        claims.put(TOKEN_TYPE, TOKEN_TYPE_ACCESS);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuer(issuer) // 설정된 이메일을 발급자로 추가
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();

    }

    private String generateClientHash(String email) {
        return DigestUtils.sha256Hex(email + new Date());
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get(AUTHORITIES_KEY, String.class);
    }

    public boolean validateToken(String token) {
        try {
            // 1. 토큰의 Claims 파싱
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 2. 토큰 타입 검증
            if (!TOKEN_TYPE_ACCESS.equals(claims.get(TOKEN_TYPE))) {
                log.error("Invalid token type: {}", claims.get(TOKEN_TYPE));
                return false;
            }

            // 3. 이메일 클레임 검증
            String email = claims.get(EMAIL_KEY, String.class);
            if (email == null || email.isEmpty()) {
                log.error("Email claim is missing or empty");
                return false;
            }

            // 4. 필수 클레임 검증
            if (claims.get("tokenId") == null || claims.get("clientHash") == null) {
                log.error("Missing security claims: tokenId or clientHash");
                return false;
            }

            // 5. 발급자 검증 (issuer)
            if (!"your-issuer@example.com".equals(claims.getIssuer())) {
                log.error("Invalid issuer: {}", claims.getIssuer());
                return false;
            }

            // 6. 토큰 만료 여부 검증
            if (isTokenExpired(claims)) {
                log.error("Token is expired");
                return false;
            }

            // 7. 모든 검증 통과
            return true;

        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }


    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateRefreshToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(EMAIL_KEY, email);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 604800000L)) // 7일 (밀리초)
                .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

}