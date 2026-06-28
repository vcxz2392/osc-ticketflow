package com.ticketflow.common.security;

import com.ticketflow.user.domain.Role;
import com.ticketflow.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${ticketflow.security.jwt.secret}") String secret,
            @Value("${ticketflow.security.jwt.expiration-ms}") long expirationMs) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret 은 최소 32바이트(256비트) 이상이어야 합니다.");
        }
        if (expirationMs <= 0) {
            throw new IllegalStateException("JWT expiration-ms 는 양수여야 합니다.");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String createToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("companyId", user.getCompany().getId())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        String username = claims.getSubject();
        Long userId = claims.get("userId", Long.class);
        Long companyId = claims.get("companyId", Long.class);
        String roleClaim = claims.get("role", String.class);
        if (username == null || userId == null || companyId == null || roleClaim == null) {
            throw new JwtException("토큰 필수 클레임 누락");
        }
        Role role;
        try {
            role = Role.valueOf(roleClaim);
        } catch (IllegalArgumentException e) {
            throw new JwtException("알 수 없는 역할: " + roleClaim);
        }
        AuthUser principal = new AuthUser(userId, companyId, username, role);
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }
}
