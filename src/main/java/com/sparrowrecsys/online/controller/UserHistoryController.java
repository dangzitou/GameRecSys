package com.sparrowrecsys.online.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparrowrecsys.online.service.RatingService;
import com.sparrowrecsys.online.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户历史记录控制器 - 获取用户评分过的游戏
 */
public class UserHistoryController extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(UserHistoryController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
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

            // 调用 Service 获取历史记录
            result = RatingService.getInstance().getUserRatingHistory(userId);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(objectMapper.writeValueAsString(result));

        } catch (Exception e) {
            logger.error("Error processing user history request", e);
            result = new HashMap<>();
            result.put("success", false);
            result.put("message", "服务器内部错误: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(objectMapper.writeValueAsString(result));
        }
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
