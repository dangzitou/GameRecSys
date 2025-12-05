package com.sparrowrecsys.online.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    
    // 密钥（生产环境应从配置文件读取）
    private static final String SECRET_KEY = "GameRecSysSecretKeyForJWT2024!@#$%^&*()VeryLongKeyHere";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    
    // Token 有效期（7天）
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000L;
    
    // Token 前缀
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";
    
    /**
     * 生成 JWT Token
     */
    public static String generateToken(Integer userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 解析 Token 获取 Claims
     */
    public static Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("Token expired: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            logger.error("Token parsing error: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 从 Token 获取用户ID
     */
    public static Integer getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Integer.class);
    }
    
    /**
     * 从 Token 获取用户名
     */
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }
    
    /**
     * 验证 Token 是否有效
     */
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
    
    /**
     * 从 Authorization Header 提取 Token
     */
    public static String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            return authHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}

