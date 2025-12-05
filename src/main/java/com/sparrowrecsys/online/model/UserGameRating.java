package com.sparrowrecsys.online.model;

import java.sql.Timestamp;

/**
 * 用户游戏评分实体类
 */
public class UserGameRating {
    private Integer id;
    private Integer userId;
    private Integer gameId;
    private Double rating;
    private Long timestamp;
    private Timestamp createdAt;

    public UserGameRating() {
    }

    public UserGameRating(Integer userId, Integer gameId, Double rating) {
        this.userId = userId;
        this.gameId = gameId;
        this.rating = rating;
        this.timestamp = System.currentTimeMillis() / 1000;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}

