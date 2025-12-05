package com.sparrowrecsys.online.datamanager;

import com.sparrowrecsys.online.util.Config;
import com.sparrowrecsys.online.util.Utility;

import java.io.File;
import java.util.*;

/**
 * DataManager is an utility class, takes charge of all data loading logic.
 */

public class DataManager {
    // singleton instance
    private static volatile DataManager instance;
    HashMap<Integer, Movie> movieMap;
    HashMap<Integer, User> userMap;
    // genre reverse index for quick querying all movies in a genre
    HashMap<String, List<Movie>> genreReverseIndexMap;

    private DataManager() {
        this.movieMap = new HashMap<>();
        this.userMap = new HashMap<>();
        this.genreReverseIndexMap = new HashMap<>();
        instance = this;
    }

    public static DataManager getInstance() {
        if (null == instance) {
            synchronized (DataManager.class) {
                if (null == instance) {
                    instance = new DataManager();
                }
            }
        }
        return instance;
    }

    // load data from file system including movie, rating, link data and model data
    // like embedding vectors.
    public void loadData(String movieDataPath, String linkDataPath, String ratingDataPath, String movieEmbPath,
            String userEmbPath, String movieRedisKey, String userRedisKey) throws Exception {
        loadMovieData(movieDataPath);

        if (linkDataPath != null && !linkDataPath.isEmpty() && new File(linkDataPath).exists()) {
            loadLinkData(linkDataPath);
        }
        if (ratingDataPath != null && !ratingDataPath.isEmpty() && new File(ratingDataPath).exists()) {
            loadRatingData(ratingDataPath);
        }
        if (movieEmbPath != null && !movieEmbPath.isEmpty() && new File(movieEmbPath).exists()) {
            loadMovieEmb(movieEmbPath, movieRedisKey);
        }

        if (Config.IS_LOAD_ITEM_FEATURE_FROM_REDIS) {
            loadMovieFeatures("mf:");
        }

        if (userEmbPath != null && !userEmbPath.isEmpty() && new File(userEmbPath).exists()) {
            loadUserEmb(userEmbPath, userRedisKey);
        }
    }

    // load movie data from movies.csv
    private void loadMovieData(String movieDataPath) throws Exception {
        System.out.println("Loading game data from " + movieDataPath + " ...");
        boolean skipFirstLine = true;
        int successCount = 0;
        int failCount = 0;
        try (Scanner scanner = new Scanner(new File(movieDataPath), "UTF-8")) {
            StringBuilder currentRecord = new StringBuilder();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (currentRecord.length() > 0) {
                    currentRecord.append("\n");
                }
                currentRecord.append(line);

                // Continue reading if quotes are not balanced (multi-line field)
                if (!isQuoteBalanced(currentRecord.toString())) {
                    continue;
                }

                String fullRecord = currentRecord.toString();
                currentRecord.setLength(0); // Reset buffer

                if (skipFirstLine) {
                    skipFirstLine = false;
                    continue;
                }

                try {
                    String[] movieData = parseCSVLine(fullRecord);
                    // Expected columns based on games_filtered.csv:
                    // AppID(0),Name(1),Release date(2),...,About the game(9),...,Header
                    // image(13),...,Positive(23),...,Genres(36),...,Screenshots(38),Movies(39)
                    if (movieData.length > 39) {
                        Movie movie = new Movie();
                        movie.setMovieId(Integer.parseInt(movieData[0]));
                        movie.setTitle(movieData[1]);

                        // Release Year parsing from "Feb 20, 2015" or "2015"
                        String releaseDate = movieData[2].replace("\"", "");
                        if (releaseDate.length() >= 4) {
                            try {
                                String yearStr = releaseDate.substring(releaseDate.length() - 4);
                                movie.setReleaseYear(Integer.parseInt(yearStr));
                            } catch (NumberFormatException e) {
                                movie.setReleaseYear(0);
                            }
                        }

                        movie.setReleaseDate(movieData[2]);
                        movie.setPrice(movieData[6]);
                        movie.setSupportedLanguages(movieData[10]);
                        movie.setDeveloper(movieData[33]);
                        movie.setPublisher(movieData[34]);

                        movie.setDescription(movieData[9]);
                        movie.setHeaderImage(movieData[13]);

                        try {
                            // Remove commas from numbers if present (e.g. "1,234")
                            String posStr = movieData[23].replace(",", "").trim();
                            movie.setPositiveReviews(Integer.parseInt(posStr));
                        } catch (NumberFormatException e) {
                            movie.setPositiveReviews(0);
                        }

                        String genres = movieData[36];
                        if (!genres.trim().isEmpty()) {
                            String[] genreArray = genres.split(",");
                            for (String genre : genreArray) {
                                String cleanGenre = genre.trim();
                                movie.addGenre(cleanGenre);
                                addMovie2GenreIndex(cleanGenre, movie);
                            }
                        }

                        movie.setScreenshots(movieData[38]);
                        movie.setProductionVideos(movieData[39]);

                        this.movieMap.put(movie.getMovieId(), movie);
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                    if (failCount <= 5) {
                        System.err.println("Error parsing line: "
                                + fullRecord.substring(0, Math.min(fullRecord.length(), 100)) + "...");
                        System.err.println(e.getMessage());
                    }
                }
            }
        }
        System.out.println("Loading game data completed. " + successCount + " games loaded. " + failCount + " failed.");
        System.out.println("Loaded Genres: " + this.genreReverseIndexMap.keySet());
    }

    private boolean isQuoteBalanced(String str) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == '\"') {
                count++;
            }
        }
        return count % 2 == 0;
    }

    // Helper to parse CSV line with quotes
    private String[] parseCSVLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    // Escaped quote
                    sb.append('\"');
                    i++; // Skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }

    // load movie embedding
    private void loadMovieEmb(String movieEmbPath, String embKey) throws Exception {
        if (Config.EMB_DATA_SOURCE.equals(Config.DATA_SOURCE_FILE)) {
            System.out.println("Loading movie embedding from " + movieEmbPath + " ...");
            int validEmbCount = 0;
            try (Scanner scanner = new Scanner(new File(movieEmbPath))) {
                while (scanner.hasNextLine()) {
                    String movieRawEmbData = scanner.nextLine();
                    String[] movieEmbData = movieRawEmbData.split(":");
                    if (movieEmbData.length == 2) {
                        Movie m = getMovieById(Integer.parseInt(movieEmbData[0]));
                        if (null == m) {
                            continue;
                        }
                        m.setEmb(Utility.parseEmbStr(movieEmbData[1]));
                        validEmbCount++;
                    }
                }
            }
            System.out.println("Loading movie embedding completed. " + validEmbCount + " movie embeddings in total.");
        } else {
            System.out.println("Loading movie embedding from Redis ...");
            Set<String> movieEmbKeys = RedisClient.getInstance().keys(embKey + "*");
            int validEmbCount = 0;
            for (String movieEmbKey : movieEmbKeys) {
                String movieId = movieEmbKey.split(":")[1];
                Movie m = getMovieById(Integer.parseInt(movieId));
                if (null == m) {
                    continue;
                }
                m.setEmb(Utility.parseEmbStr(RedisClient.getInstance().get(movieEmbKey)));
                validEmbCount++;
            }
            System.out.println("Loading movie embedding completed. " + validEmbCount + " movie embeddings in total.");
        }
    }

    // load movie features
    private void loadMovieFeatures(String movieFeaturesPrefix) throws Exception {
        System.out.println("Loading movie features from Redis ...");
        Set<String> movieFeaturesKeys = RedisClient.getInstance().keys(movieFeaturesPrefix + "*");
        int validFeaturesCount = 0;
        for (String movieFeaturesKey : movieFeaturesKeys) {
            String movieId = movieFeaturesKey.split(":")[1];
            Movie m = getMovieById(Integer.parseInt(movieId));
            if (null == m) {
                continue;
            }
            m.setMovieFeatures(RedisClient.getInstance().hgetAll(movieFeaturesKey));
            validFeaturesCount++;
        }
        System.out.println("Loading movie features completed. " + validFeaturesCount + " movie features in total.");
    }

    // load user embedding
    private void loadUserEmb(String userEmbPath, String embKey) throws Exception {
        if (Config.EMB_DATA_SOURCE.equals(Config.DATA_SOURCE_FILE)) {
            System.out.println("Loading user embedding from " + userEmbPath + " ...");
            int validEmbCount = 0;
            try (Scanner scanner = new Scanner(new File(userEmbPath))) {
                while (scanner.hasNextLine()) {
                    String userRawEmbData = scanner.nextLine();
                    String[] userEmbData = userRawEmbData.split(":");
                    if (userEmbData.length == 2) {
                        User u = getUserById(Integer.parseInt(userEmbData[0]));
                        if (null == u) {
                            continue;
                        }
                        u.setEmb(Utility.parseEmbStr(userEmbData[1]));
                        validEmbCount++;
                    }
                }
            }
            System.out.println("Loading user embedding completed. " + validEmbCount + " user embeddings in total.");
        }
    }

    // parse release year
    private int parseReleaseYear(String rawTitle) {
        if (null == rawTitle || rawTitle.trim().length() < 6) {
            return -1;
        } else {
            String yearString = rawTitle.trim().substring(rawTitle.length() - 5, rawTitle.length() - 1);
            try {
                return Integer.parseInt(yearString);
            } catch (NumberFormatException exception) {
                return -1;
            }
        }
    }

    // load links data from links.csv
    private void loadLinkData(String linkDataPath) throws Exception {
        System.out.println("Loading link data from " + linkDataPath + " ...");
        int count = 0;
        boolean skipFirstLine = true;
        try (Scanner scanner = new Scanner(new File(linkDataPath))) {
            while (scanner.hasNextLine()) {
                String linkRawData = scanner.nextLine();
                if (skipFirstLine) {
                    skipFirstLine = false;
                    continue;
                }
                String[] linkData = linkRawData.split(",");
                if (linkData.length == 3) {
                    int movieId = Integer.parseInt(linkData[0]);
                    Movie movie = this.movieMap.get(movieId);
                    if (null != movie) {
                        count++;
                        movie.setImdbId(linkData[1].trim());
                        movie.setTmdbId(linkData[2].trim());
                    }
                }
            }
        }
        System.out.println("Loading link data completed. " + count + " links in total.");
    }

    // load ratings data from ratings.csv
    private void loadRatingData(String ratingDataPath) throws Exception {
        System.out.println("Loading rating data from " + ratingDataPath + " ...");
        boolean skipFirstLine = true;
        int count = 0;
        try (Scanner scanner = new Scanner(new File(ratingDataPath))) {
            while (scanner.hasNextLine()) {
                String ratingRawData = scanner.nextLine();
                if (skipFirstLine) {
                    skipFirstLine = false;
                    continue;
                }
                String[] linkData = ratingRawData.split(",");
                if (linkData.length == 4) {
                    count++;
                    Rating rating = new Rating();
                    rating.setUserId(Integer.parseInt(linkData[0]));
                    rating.setMovieId(Integer.parseInt(linkData[1]));
                    rating.setScore(Float.parseFloat(linkData[2]));
                    rating.setTimestamp(Long.parseLong(linkData[3]));
                    Movie movie = this.movieMap.get(rating.getMovieId());
                    if (null != movie) {
                        movie.addRating(rating);
                    }
                    if (!this.userMap.containsKey(rating.getUserId())) {
                        User user = new User();
                        user.setUserId(rating.getUserId());
                        this.userMap.put(user.getUserId(), user);
                    }
                    this.userMap.get(rating.getUserId()).addRating(rating);
                }
            }
        }

        System.out.println("Loading rating data completed. " + count + " ratings in total.");
    }

    // add movie to genre reversed index
    private void addMovie2GenreIndex(String genre, Movie movie) {
        if (!this.genreReverseIndexMap.containsKey(genre)) {
            this.genreReverseIndexMap.put(genre, new ArrayList<>());
        }
        this.genreReverseIndexMap.get(genre).add(movie);
    }

    // get movies by genre, and order the movies by sortBy method
    public List<Movie> getMoviesByGenre(String genre, int size, String sortBy) {
        if (null != genre) {
            List<Movie> movies = this.genreReverseIndexMap.get(genre);
            if (null == movies) {
                return new ArrayList<>();
            }
            movies = new ArrayList<>(movies);
            switch (sortBy) {
                case "rating":
                    movies.sort((m1, m2) -> Double.compare(m2.getAverageRating(), m1.getAverageRating()));
                    break;
                case "releaseYear":
                    movies.sort((m1, m2) -> Integer.compare(m2.getReleaseYear(), m1.getReleaseYear()));
                    break;
                case "positiveReviews":
                    movies.sort((m1, m2) -> Integer.compare(m2.getPositiveReviews(), m1.getPositiveReviews()));
                    break;
                default:
            }

            if (movies.size() > size) {
                return movies.subList(0, size);
            }
            return movies;
        }
        return null;
    }

    // get top N movies order by sortBy method
    public List<Movie> getMovies(int size, String sortBy) {
        List<Movie> movies = new ArrayList<>(movieMap.values());
        switch (sortBy) {
            case "rating":
                movies.sort((m1, m2) -> Double.compare(m2.getAverageRating(), m1.getAverageRating()));
                break;
            case "releaseYear":
                movies.sort((m1, m2) -> Integer.compare(m2.getReleaseYear(), m1.getReleaseYear()));
                break;
            case "positiveReviews":
                movies.sort((m1, m2) -> Integer.compare(m2.getPositiveReviews(), m1.getPositiveReviews()));
                break;
            default:
        }

        if (movies.size() > size) {
            return movies.subList(0, size);
        }
        return movies;
    }

    public List<Movie> searchMovies(String query, int size) {
        List<Movie> result = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return result;
        }
        String lowerQuery = query.toLowerCase();
        for (Movie movie : movieMap.values()) {
            if (movie.getTitle().toLowerCase().contains(lowerQuery)) {
                result.add(movie);
            }
        }
        // Sort by popularity (positive reviews)
        result.sort((m1, m2) -> m2.getPositiveReviews() - m1.getPositiveReviews());

        if (result.size() > size) {
            return result.subList(0, size);
        }
        return result;
    }

    // get movie object by movie id
    public Movie getMovieById(int movieId) {
        return this.movieMap.get(movieId);
    }

    // get user object by user id
    public User getUserById(int userId) {
        return this.userMap.get(userId);
    }
}
