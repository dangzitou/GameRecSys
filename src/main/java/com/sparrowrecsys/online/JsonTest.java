package com.sparrowrecsys.online;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparrowrecsys.online.datamanager.DataManager;
import com.sparrowrecsys.online.datamanager.GameItem;
import java.util.List;

public class JsonTest {
    public static void main(String[] args) {
        try {
            String rootPath = "src/main/resources/webroot/";
            System.out.println("Loading Real Data...");
            DataManager.getInstance().loadData(
                    rootPath + "sampledata/games_filtered.csv",
                    rootPath + "sampledata/links.csv",
                    rootPath + "sampledata/ratings.csv",
                    rootPath + "modeldata/item2vecEmb.csv",
                    rootPath + "modeldata/userEmb.csv",
                    "i2vEmb", "uEmb");

            System.out.println("Fetching Action games...");
            List<GameItem> games = DataManager.getInstance().getGamesByGenre("Action", 10, "positiveReviews");

            if (games == null || games.isEmpty()) {
                System.out.println("No games found!");
                return;
            }

            System.out.println("Serializing " + games.size() + " games...");
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(games);
            System.out.println("Serialization Success! Length: " + json.length());
            // System.out.println(json.substring(0, 500)); // Print snippet

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
