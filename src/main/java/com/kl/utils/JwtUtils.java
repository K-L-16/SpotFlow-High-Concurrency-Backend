package com.kl.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtils {

    // 至少 32 字节，HS256/HS384/HS512 都要求足够强的 key
    private static final String SECRET = "qwertyuioasdfghjkzxcghmfheiawofaohfwa";
    private static final long EXPIRE_MILLIS = 1000L * 60 * 60 * 24 * 3; // 3天

    private static final SecretKey KEY =
            Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public static String generateToken(Long userId) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + EXPIRE_MILLIS);

        return Jwts.builder()
                .subject("login")
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(expire)
                .signWith(KEY)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        if (userId instanceof Long) {
            return (Long) userId;
        }
        return Long.valueOf(String.valueOf(userId));
    }

    public static Date getExpiration(String token) {
        return parseToken(token).getExpiration();
    }
}