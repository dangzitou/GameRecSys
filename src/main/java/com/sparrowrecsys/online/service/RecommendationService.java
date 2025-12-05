package com.sparrowrecsys.online.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparrowrecsys.online.model.Game;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

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

            System.out.println("RecommendationService: genre=" + genre + ", size=" + size + ", sortby=" + sortby);

            // fetch games from DB via GameService
            List<Game> games = GameService.getInstance().getGamesByGenre(genre, Integer.parseInt(size), sortby);

            if (games == null || games.isEmpty()) {
                System.out.println("RecommendationService: No games found for genre " + genre);
            } else {
                System.out.println("RecommendationService: Found " + games.size() + " games.");
            }

            // convert game list to json format and return
            ObjectMapper mapper = new ObjectMapper();
            String jsonGames = mapper.writeValueAsString(games);
            response.getWriter().println(jsonGames);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error: " + e.getMessage());
        }
    }
}
