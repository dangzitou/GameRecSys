package com.sparrowrecsys.online.service;

import com.sparrowrecsys.online.mapper.UserGameRatingMapper;
import com.sparrowrecsys.online.model.UserGameRating;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评分服务类
 */
public class RatingService {
    private static final Logger logger = LoggerFactory.getLogger(RatingService.class);
    private static volatile RatingService instance;
    private SqlSessionFactory sqlSessionFactory;

    private RatingService() {
        try {
            String resource = "mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            logger.info("RatingService SqlSessionFactory initialized successfully.");
        } catch (IOException e) {
            logger.error("Error initializing RatingService SqlSessionFactory", e);
            throw new RuntimeException("Error initializing RatingService SqlSessionFactory", e);
        }
    }

    public static RatingService getInstance() {
        if (null == instance) {
            synchronized (RatingService.class) {
                if (null == instance) {
                    instance = new RatingService();
                }
            }
        }
        return instance;
    }

    /**
     * 提交或更新评分
     */
    public Map<String, Object> submitRating(Integer userId, Integer gameId, Double rating) {
        Map<String, Object> result = new HashMap<>();

        if (rating < 1.0 || rating > 5.0) {
            result.put("success", false);
            result.put("message", "评分必须在1.0到5.0之间");
            return result;
        }

        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserGameRatingMapper mapper = session.getMapper(UserGameRatingMapper.class);

            UserGameRating userRating = new UserGameRating(userId, gameId, rating);
            mapper.upsert(userRating);
            session.commit();

            // 获取游戏的平均评分和评分数
            Double avgRating = mapper.getAverageRatingByGameId(gameId);
            Integer ratingCount = mapper.getRatingCountByGameId(gameId);

            result.put("success", true);
            result.put("message", "评分成功");
            result.put("userRating", rating);
            result.put("averageRating", avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0);
            result.put("ratingCount", ratingCount != null ? ratingCount : 0);

            logger.info("User {} rated game {} with score {}", userId, gameId, rating);
        } catch (Exception e) {
            logger.error("Submit rating failed for user {} game {}", userId, gameId, e);
            result.put("success", false);
            result.put("message", "评分失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取用户对某游戏的评分
     */
    public Map<String, Object> getUserRating(Integer userId, Integer gameId) {
        Map<String, Object> result = new HashMap<>();

        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserGameRatingMapper mapper = session.getMapper(UserGameRatingMapper.class);

            UserGameRating userRating = mapper.selectByUserAndGame(userId, gameId);
            Double avgRating = mapper.getAverageRatingByGameId(gameId);
            Integer ratingCount = mapper.getRatingCountByGameId(gameId);

            result.put("success", true);
            result.put("userRating", userRating != null ? userRating.getRating() : null);
            result.put("averageRating", avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0);
            result.put("ratingCount", ratingCount != null ? ratingCount : 0);
        } catch (Exception e) {
            logger.error("Get user rating failed for user {} game {}", userId, gameId, e);
            result.put("success", false);
            result.put("message", "获取评分失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取游戏的评分统计
     */
    public Map<String, Object> getGameRatingStats(Integer gameId) {
        Map<String, Object> result = new HashMap<>();

        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserGameRatingMapper mapper = session.getMapper(UserGameRatingMapper.class);

            Double avgRating = mapper.getAverageRatingByGameId(gameId);
            Integer ratingCount = mapper.getRatingCountByGameId(gameId);

            result.put("success", true);
            result.put("averageRating", avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0);
            result.put("ratingCount", ratingCount != null ? ratingCount : 0);
        } catch (Exception e) {
            logger.error("Get game rating stats failed for game {}", gameId, e);
            result.put("success", false);
            result.put("message", "获取评分统计失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取用户的所有评分
     */
    public Map<String, Object> getUserRatings(Integer userId) {
        Map<String, Object> result = new HashMap<>();

        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserGameRatingMapper mapper = session.getMapper(UserGameRatingMapper.class);
            List<UserGameRating> ratings = mapper.selectByUserId(userId);

            result.put("success", true);
            result.put("ratings", ratings);
            result.put("count", ratings.size());
        } catch (Exception e) {
            logger.error("Get user ratings failed for user {}", userId, e);
            result.put("success", false);
            result.put("message", "获取用户评分失败: " + e.getMessage());
        }

        return result;
    }
}

