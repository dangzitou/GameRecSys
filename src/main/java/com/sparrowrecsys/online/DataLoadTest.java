package com.sparrowrecsys.online;

import com.sparrowrecsys.online.datamanager.DataManager;
import com.sparrowrecsys.online.datamanager.Movie;
import java.util.List;
import java.io.File;

public class DataLoadTest {
    public static void main(String[] args) {
        try {
            String rootPath = "src/main/resources/webroot/";
            System.out.println("Testing Data Loading...");
            
            DataManager.getInstance().loadData(
                rootPath + "sampledata/games_filtered.csv",
                rootPath + "sampledata/links.csv",
                rootPath + "sampledata/ratings.csv",
                rootPath + "modeldata/item2vecEmb.csv",
                rootPath + "modeldata/userEmb.csv",
                "i2vEmb", "uEmb"
            );
            
            // Check Action Genre
            List<Movie> actionMovies = DataManager.getInstance().getMoviesByGenre("Action", 10, "positiveReviews");
            if (actionMovies != null) {
                System.out.println("Found " + actionMovies.size() + " Action games.");
                for (Movie m : actionMovies) {
                    System.out.println(" - " + m.getTitle() + " (" + m.getPositiveReviews() + ")");
                }
            } else {
                System.out.println("Action genre returned NULL.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
