package com.sparrowrecsys.online.mapper;

import com.sparrowrecsys.online.model.UserGameRating;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户游戏评分 Mapper 接口
 */
public interface UserGameRatingMapper {

    /**
     * 插入评分
     */
    int insert(UserGameRating rating);

    /**
     * 更新评分
     */
    int update(UserGameRating rating);

    /**
     * 插入或更新评分 (UPSERT)
     */
    int upsert(UserGameRating rating);

    /**
     * 根据用户ID和游戏ID查询评分
     */
    UserGameRating selectByUserAndGame(@Param("userId") Integer userId, @Param("gameId") Integer gameId);

    /**
     * 查询用户的所有评分
     */
    List<UserGameRating> selectByUserId(@Param("userId") Integer userId);

    /**
     * 查询游戏的所有评分
     */
    List<UserGameRating> selectByGameId(@Param("gameId") Integer gameId);

    /**
     * 删除评分
     */
    int deleteByUserAndGame(@Param("userId") Integer userId, @Param("gameId") Integer gameId);

    /**
     * 获取游戏的平均评分
     */
    Double getAverageRatingByGameId(@Param("gameId") Integer gameId);

    /**
     * 获取游戏的评分数量
     */
    Integer getRatingCountByGameId(@Param("gameId") Integer gameId);

    /**
     * 获取所有评分数据（用于导出训练数据）
     */
    List<UserGameRating> selectAll();

    /**
     * 分页获取评分数据
     */
    List<UserGameRating> selectWithPagination(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 获取评分总数
     */
    int countAll();
}

