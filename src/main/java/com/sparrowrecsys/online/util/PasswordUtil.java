package com.sparrowrecsys.online.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码加密工具类
 * 使用 SHA-256 + Salt 进行密码加密
 */
public class PasswordUtil {
    private static final int SALT_LENGTH = 16;
    private static final String ALGORITHM = "SHA-256";
    private static final String SEPARATOR = ":";

    /**
     * 对密码进行加密（生成盐值 + 哈希）
     */
    public static String hashPassword(String password) {
        try {
            // 生成随机盐值
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // 计算哈希
            byte[] hash = hash(password, salt);
            
            // 返回 salt:hash 格式
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);
            
            return saltBase64 + SEPARATOR + hashBase64;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * 验证密码
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // 分离盐值和哈希
            String[] parts = storedHash.split(SEPARATOR);
            if (parts.length != 2) {
                return false;
            }
            
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
            
            // 计算输入密码的哈希
            byte[] actualHash = hash(password, salt);
            
            // 比较哈希值
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] hash(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(ALGORITHM);
        md.update(salt);
        return md.digest(password.getBytes(StandardCharsets.UTF_8));
    }
}

