package com.sparrowrecsys.online.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.sparrowrecsys.online.model.Game;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RecommendationService, provide recommendation service based on different
 * input
 */

public class RecommendationService extends HttpServlet {
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException,
            IOException {
        try {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Access-Control-Allow-Origin", "*");

            // genre - movie category
            String genre = request.getParameter("genre");
            // number of returned movies
            String size = request.getParameter("size");
            // ranking algorithm
            String sortby = request.getParameter("sortby");
            // page number
            String page = request.getParameter("page");
            int pageNum = (page == null || page.isEmpty()) ? 1 : Integer.parseInt(page);
            int pageSize = (size == null || size.isEmpty()) ? 10 : Integer.parseInt(size);

            System.out.println("RecommendationService: genre=" + genre + ", page=" + pageNum + ", size=" + pageSize
                    + ", sortby=" + sortby);

            // fetch games from DB via GameService
            List<Game> games = GameService.getInstance().getGamesByGenre(genre, pageNum, pageSize, sortby);
            PageInfo<Game> pageInfo = new PageInfo<>(games);

            if (games == null || games.isEmpty()) {
                System.out.println("RecommendationService: No games found for genre " + genre);
            } else {
                System.out.println(
                        "RecommendationService: Found " + games.size() + " games. Total pages: " + pageInfo.getPages());
            }

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("games", games);
            responseMap.put("totalPages", pageInfo.getPages());
            responseMap.put("total", pageInfo.getTotal());
            responseMap.put("currentPage", pageInfo.getPageNum());

            // convert map to json format and return
            ObjectMapper mapper = new ObjectMapper();
            String jsonOutput = mapper.writeValueAsString(responseMap);
            response.getWriter().println(jsonOutput);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error: " + e.getMessage());
        }
    }
}
