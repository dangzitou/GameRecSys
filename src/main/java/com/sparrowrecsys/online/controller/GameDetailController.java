package com.sparrowrecsys.online.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparrowrecsys.online.model.Game;
import com.sparrowrecsys.online.service.GameService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * GameDetailController, return information of a specific game
 */
public class GameDetailController extends HttpServlet {
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        try {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Access-Control-Allow-Origin", "*");

            // get game id via url parameter
            String gameId = request.getParameter("id");

            // get game object from GameService (DB)
            Game game = GameService.getInstance().getGameById(Integer.parseInt(gameId));

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

