package com.sparrowrecsys.online.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparrowrecsys.online.service.RatingService;
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
 * 评分控制器 - 处理用户评分请求
 */
public class RatingController extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(RatingController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setCorsHeaders(response);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result;

        try {
            // 验证 Token
            String authHeader = request.getHeader(JwtUtil.HEADER_NAME);
            String token = JwtUtil.extractToken(authHeader);

            if (token == null || !JwtUtil.validateToken(token)) {
                result = new HashMap<>();
                result.put("success", false);
                result.put("message", "请先登录");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(objectMapper.writeValueAsString(result));
                return;
            }

            Integer userId = JwtUtil.getUserId(token);
            Map<String, Object> requestBody = readRequestBody(request);

            Integer gameId = parseInteger(requestBody.get("gameId"));
            Double rating = parseDouble(requestBody.get("rating"));

            if (gameId == null || rating == null) {
                result = new HashMap<>();
                result.put("success", false);
                result.put("message", "gameId和rating参数不能为空");
                response.getWriter().write(objectMapper.writeValueAsString(result));
                return;
            }

            result = RatingService.getInstance().submitRating(userId, gameId, rating);
        } catch (Exception e) {
            logger.error("Submit rating failed", e);
            result = new HashMap<>();
            result.put("success", false);
            result.put("message", "评分失败: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setCorsHeaders(response);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result;

        try {
            String gameIdStr = request.getParameter("gameId");
            Integer gameId = gameIdStr != null ? Integer.parseInt(gameIdStr) : null;

            if (gameId == null) {
                result = new HashMap<>();
                result.put("success", false);
                result.put("message", "gameId参数不能为空");
                response.getWriter().write(objectMapper.writeValueAsString(result));
                return;
            }

            // 尝试获取用户Token
            String authHeader = request.getHeader(JwtUtil.HEADER_NAME);
            String token = JwtUtil.extractToken(authHeader);

            if (token != null && JwtUtil.validateToken(token)) {
                Integer userId = JwtUtil.getUserId(token);
                result = RatingService.getInstance().getUserRating(userId, gameId);
            } else {
                result = RatingService.getInstance().getGameRatingStats(gameId);
            }
        } catch (Exception e) {
            logger.error("Get rating failed", e);
            result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取评分失败: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private Integer parseInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        if (sb.length() == 0) return new HashMap<>();
        return objectMapper.readValue(sb.toString(), Map.class);
    }
}

