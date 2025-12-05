package com.sparrowrecsys.online.service;

import com.sparrowrecsys.online.mapper.UserAccountMapper;
import com.sparrowrecsys.online.model.UserAccount;
import com.sparrowrecsys.online.util.JwtUtil;
import com.sparrowrecsys.online.util.PasswordUtil;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务类
 */
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static volatile AuthService instance;
    private SqlSessionFactory sqlSessionFactory;

    private AuthService() {
        try {
            String resource = "mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            logger.info("AuthService SqlSessionFactory initialized successfully.");
        } catch (IOException e) {
            logger.error("Error initializing AuthService SqlSessionFactory", e);
            throw new RuntimeException("Error initializing AuthService SqlSessionFactory", e);
        }
    }

    public static AuthService getInstance() {
        if (null == instance) {
            synchronized (AuthService.class) {
                if (null == instance) {
                    instance = new AuthService();
                }
            }
        }
        return instance;
    }

    /**
     * 用户注册
     */
    public Map<String, Object> register(String username, String password, String email, String nickname) {
        Map<String, Object> result = new HashMap<>();
        
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserAccountMapper mapper = session.getMapper(UserAccountMapper.class);
            
            // 检查用户名是否已存在
            if (mapper.selectByUsername(username) != null) {
                result.put("success", false);
                result.put("message", "用户名已存在");
                return result;
            }
            
            // 检查邮箱是否已存在
            if (email != null && !email.isEmpty() && mapper.selectByEmail(email) != null) {
                result.put("success", false);
                result.put("message", "邮箱已被注册");
                return result;
            }
            
            // 创建新用户
            UserAccount user = new UserAccount();
            user.setUsername(username);
            user.setPassword(PasswordUtil.hashPassword(password));
            user.setEmail(email);
            user.setNickname(nickname != null ? nickname : username);
            user.setStatus(1);
            
            mapper.insert(user);
            session.commit();
            
            // 生成 Token
            String token = JwtUtil.generateToken(user.getId(), user.getUsername());
            
            result.put("success", true);
            result.put("message", "注册成功");
            result.put("token", token);
            result.put("user", buildUserInfo(user));
            
            logger.info("User registered successfully: {}", username);
        } catch (Exception e) {
            logger.error("Registration failed for user: {}", username, e);
            result.put("success", false);
            result.put("message", "注册失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 用户登录
     */
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();
        
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserAccountMapper mapper = session.getMapper(UserAccountMapper.class);
            
            UserAccount user = mapper.selectByUsername(username);
            
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }
            
            if (user.getStatus() == 0) {
                result.put("success", false);
                result.put("message", "账号已被禁用");
                return result;
            }
            
            if (!PasswordUtil.verifyPassword(password, user.getPassword())) {
                result.put("success", false);
                result.put("message", "密码错误");
                return result;
            }
            
            // 更新最后登录时间
            mapper.updateLastLoginTime(user.getId());
            session.commit();
            
            // 生成 Token
            String token = JwtUtil.generateToken(user.getId(), user.getUsername());
            
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("token", token);
            result.put("user", buildUserInfo(user));
            
            logger.info("User logged in successfully: {}", username);
        } catch (Exception e) {
            logger.error("Login failed for user: {}", username, e);
            result.put("success", false);
            result.put("message", "登录失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取用户信息（根据Token）
     */
    public Map<String, Object> getUserInfo(String token) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Integer userId = JwtUtil.getUserId(token);
            
            try (SqlSession session = sqlSessionFactory.openSession()) {
                UserAccountMapper mapper = session.getMapper(UserAccountMapper.class);
                UserAccount user = mapper.selectById(userId);
                
                if (user == null) {
                    result.put("success", false);
                    result.put("message", "用户不存在");
                    return result;
                }
                
                result.put("success", true);
                result.put("user", buildUserInfo(user));
            }
        } catch (Exception e) {
            logger.error("Get user info failed", e);
            result.put("success", false);
            result.put("message", "获取用户信息失败: " + e.getMessage());
        }
        
        return result;
    }

    private Map<String, Object> buildUserInfo(UserAccount user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        return userInfo;
    }
}

