package com.sparrowrecsys.online.datamanager;

/**
 * Rating Class, contains attributes loaded from ratings.csv
 */
public class Rating {
    int gameId;
    int userId;
    float score;
    long timestamp;

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    // Keep movieId getter/setter for backward compatibility
    public int getMovieId() {
        return gameId;
    }

    public void setMovieId(int movieId) {
        this.gameId = movieId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
