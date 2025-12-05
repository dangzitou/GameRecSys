package com.sparrowrecsys.online.mapper;

import com.sparrowrecsys.online.model.Game;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface GameMapper {
    Game selectGameById(@Param("appId") int appId);

    List<Game> selectAllGames();

    List<Game> searchGames(@Param("query") String query, @Param("limit") int limit);

    List<Game> selectGamesByGenre(@Param("genre") String genre, @Param("sortBy") String sortBy);
}
