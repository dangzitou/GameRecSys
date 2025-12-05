package com.sparrowrecsys.online.service;

import com.sparrowrecsys.online.mapper.GameMapper;
import com.sparrowrecsys.online.model.Game;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class GameService {
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);
    private static volatile GameService instance;
    private SqlSessionFactory sqlSessionFactory;

    private GameService() {
        try {
            String resource = "mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            logger.info("MyBatis SqlSessionFactory initialized successfully.");
        } catch (IOException e) {
            logger.error("Error initializing MyBatis SqlSessionFactory", e);
            throw new RuntimeException("Error initializing MyBatis SqlSessionFactory", e);
        }
    }

    public static GameService getInstance() {
        if (null == instance) {
            synchronized (GameService.class) {
                if (null == instance) {
                    instance = new GameService();
                }
            }
        }
        return instance;
    }

    public Game getGameById(int appId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GameMapper mapper = session.getMapper(GameMapper.class);
            Game game = mapper.selectGameById(appId);
            logger.info("getGameById: appId={}, found={}", appId, game != null);
            return game;
        }
    }

    public List<Game> searchGames(String query, int limit) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GameMapper mapper = session.getMapper(GameMapper.class);
            List<Game> games = mapper.searchGames(query, limit);
            logger.info("searchGames: query='{}', limit={}, resultSize={}", query, limit, games.size());
            return games;
        }
    }

    public List<Game> getGamesByGenre(String genre, int limit) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GameMapper mapper = session.getMapper(GameMapper.class);
            List<Game> games = mapper.selectGamesByGenre(genre, limit);
            logger.info("getGamesByGenre: genre='{}', limit={}, resultSize={}", genre, limit, games.size());
            return games;
        }
    }
}