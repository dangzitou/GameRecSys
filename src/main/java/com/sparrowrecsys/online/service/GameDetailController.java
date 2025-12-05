package com.sparrowrecsys.online.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparrowrecsys.online.model.Game;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * MovieService, return information of a specific movie
 */

public class GameDetailController extends HttpServlet {
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        try {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Access-Control-Allow-Origin", "*");

            // get movie id via url parameter
            String movieId = request.getParameter("id");

            // get game object from GameService (DB)
            Game game = GameService.getInstance().getGameById(Integer.parseInt(movieId));

            // convert game object to json format and return
            if (null != game) {
                ObjectMapper mapper = new ObjectMapper();
                String jsonGame = mapper.writeValueAsString(game);
                response.getWriter().println(jsonGame);
            } else {
                response.getWriter().println("{}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("{}");
        }
    }
}
