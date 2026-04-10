package com.kl.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtils {



    public static String generateToken(Long userId, String secret, long expireMillis) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + expireMillis);
        SecretKey KEY = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject("login")
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(expire)
                .signWith(KEY)
                .compact();
    }

    public static Claims parseToken(String token, String secret) {
        SecretKey KEY = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static Long getUserId(String token, String secret) {
        Claims claims = parseToken(token, secret);
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        if (userId instanceof Long) {
            return (Long) userId;
        }
        return Long.valueOf(String.valueOf(userId));
    }

    public static Date getExpiration(String token, String secret) {
        return parseToken(token, secret).getExpiration();
    }
}