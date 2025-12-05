package com.sparrowrecsys.online.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparrowrecsys.online.service.AuthService;
import com.sparrowrecsys.online.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器 - 处理登录和注册请求
 */
public class AuthController extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        
        String pathInfo = request.getPathInfo();
        Map<String, Object> result;
        
        try {
            // 读取请求体
            Map<String, String> requestBody = readRequestBody(request);
            
            if ("/register".equals(pathInfo)) {
                result = handleRegister(requestBody);
            } else if ("/login".equals(pathInfo)) {
                result = handleLogin(requestBody);
            } else {
                result = new HashMap<>();
                result.put("success", false);
                result.put("message", "未知的操作");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Auth request failed", e);
            result = new HashMap<>();
            result.put("success", false);
            result.put("message", "请求处理失败: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        
        String pathInfo = request.getPathInfo();
        Map<String, Object> result;
        
        try {
            if ("/userinfo".equals(pathInfo)) {
                result = handleGetUserInfo(request);
            } else {
                result = new HashMap<>();
                result.put("success", false);
                result.put("message", "未知的操作");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Auth request failed", e);
            result = new HashMap<>();
            result.put("success", false);
            result.put("message", "请求处理失败: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private Map<String, Object> handleRegister(Map<String, String> requestBody) {
        String username = requestBody.get("username");
        String password = requestBody.get("password");
        String email = requestBody.get("email");
        String nickname = requestBody.get("nickname");
        
        // 参数验证
        if (username == null || username.trim().isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "用户名不能为空");
            return result;
        }
        
        if (password == null || password.length() < 6) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "密码长度不能少于6位");
            return result;
        }
        
        return AuthService.getInstance().register(username.trim(), password, email, nickname);
    }

    private Map<String, Object> handleLogin(Map<String, String> requestBody) {
        String username = requestBody.get("username");
        String password = requestBody.get("password");
        
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "用户名和密码不能为空");
            return result;
        }
        
        return AuthService.getInstance().login(username.trim(), password);
    }

    private Map<String, Object> handleGetUserInfo(HttpServletRequest request) {
        String authHeader = request.getHeader(JwtUtil.HEADER_NAME);
        String token = JwtUtil.extractToken(authHeader);
        
        if (token == null || !JwtUtil.validateToken(token)) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "无效的令牌");
            return result;
        }
        
        return AuthService.getInstance().getUserInfo(token);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        
        if (sb.length() == 0) {
            return new HashMap<>();
        }
        
        return objectMapper.readValue(sb.toString(), Map.class);
    }
}

