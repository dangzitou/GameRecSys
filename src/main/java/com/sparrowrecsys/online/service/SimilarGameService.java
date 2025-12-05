package com.sparrowrecsys.online.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparrowrecsys.online.datamanager.GameItem;
import com.sparrowrecsys.online.recprocess.SimilarGameProcess;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * SimilarGameService, recommend similar games given by a specific game
 */
public class SimilarGameService extends HttpServlet {
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        try {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Access-Control-Allow-Origin", "*");

            // gameId
            String gameId = request.getParameter("gameId");
            // number of returned games
            String size = request.getParameter("size");
            // model of calculating similarity, e.g. embedding, graph-embedding
            String model = request.getParameter("model");

            // use SimilarGameProcess to get similar games
            List<GameItem> games = SimilarGameProcess.getRecList(Integer.parseInt(gameId), Integer.parseInt(size),
                    model);

            // convert game list to json format and return
            ObjectMapper mapper = new ObjectMapper();
            String jsonGames = mapper.writeValueAsString(games);
            response.getWriter().println(jsonGames);

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("");
        }
    }
}