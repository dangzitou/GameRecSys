package com.sparrowrecsys.online.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparrowrecsys.online.model.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * SearchService, return games matching a query
 */
public class SearchService extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        try {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Access-Control-Allow-Origin", "*");

            String query = request.getParameter("query");
            String sizeStr = request.getParameter("size");
            int size = 20; // Default size
            if (sizeStr != null && !sizeStr.isEmpty()) {
                size = Integer.parseInt(sizeStr);
            }

            logger.info("Received search request: query='{}', size={}", query, size);

            // Use GameService to search from DB
            List<Game> games = GameService.getInstance().searchGames(query, size);

            ObjectMapper mapper = new ObjectMapper();
            String jsonGames = mapper.writeValueAsString(games);
            response.getWriter().println(jsonGames);

        } catch (Exception e) {
            logger.error("Error processing search request", e);
            e.printStackTrace();
            response.getWriter().println("[]");
        }
    }
}
