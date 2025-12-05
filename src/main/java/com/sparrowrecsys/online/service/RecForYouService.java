package com.sparrowrecsys.online.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparrowrecsys.online.recprocess.RecForYouProcess;
import com.sparrowrecsys.online.util.ABTest;
import com.sparrowrecsys.online.datamanager.GameItem;
import com.sparrowrecsys.online.util.Config;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * RecForYouService, provide recommended for you service
 */

public class RecForYouService extends HttpServlet {
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException,
            IOException {
        try {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Access-Control-Allow-Origin", "*");

            // get user id via url parameter
            String userId = request.getParameter("id");
            // number of returned games
            String size = request.getParameter("size");
            // ranking algorithm
            String model = request.getParameter("model");

            if (Config.IS_ENABLE_AB_TEST) {
                model = ABTest.getConfigByUserId(userId);
            }

            // a simple method, just fetch all the games in the genre
            List<GameItem> games = RecForYouProcess.getRecList(Integer.parseInt(userId), Integer.parseInt(size), model);

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
