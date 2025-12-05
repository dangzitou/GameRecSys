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
    HashMap<Integer, GameItem> gameMap;
    HashMap<Integer, User> userMap;
    // genre reverse index for quick querying all games in a genre
    HashMap<String, List<GameItem>> genreReverseIndexMap;

    private DataManager() {
        this.gameMap = new HashMap<>();
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

    // load data from file system including game, rating, link data and model data
    // like embedding vectors.
    public void loadData(String gameDataPath, String linkDataPath, String ratingDataPath, String gameEmbPath,
            String userEmbPath, String gameRedisKey, String userRedisKey) throws Exception {
        loadGameData(gameDataPath);

        if (linkDataPath != null && !linkDataPath.isEmpty() && new File(linkDataPath).exists()) {
            loadLinkData(linkDataPath);
        }
        if (ratingDataPath != null && !ratingDataPath.isEmpty() && new File(ratingDataPath).exists()) {
            loadRatingData(ratingDataPath);
        }
        if (gameEmbPath != null && !gameEmbPath.isEmpty() && new File(gameEmbPath).exists()) {
            loadGameEmb(gameEmbPath, gameRedisKey);
        }

        if (Config.IS_LOAD_ITEM_FEATURE_FROM_REDIS) {
            loadGameFeatures("gf:");
        }

        if (userEmbPath != null && !userEmbPath.isEmpty() && new File(userEmbPath).exists()) {
            loadUserEmb(userEmbPath, userRedisKey);
        }
    }

    // load game data from games.csv
    private void loadGameData(String gameDataPath) throws Exception {
        System.out.println("Loading game data from " + gameDataPath + " ...");
        boolean skipFirstLine = true;
        int successCount = 0;
        int failCount = 0;
        try (Scanner scanner = new Scanner(new File(gameDataPath), "UTF-8")) {
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
                    String[] gameData = parseCSVLine(fullRecord);
                    // Expected columns based on games_filtered.csv:
                    // AppID(0),Name(1),Release date(2),...,About the game(9),...,Header
                    // image(13),...,Positive(23),...,Genres(36),...,Screenshots(38),Movies(39)
                    if (gameData.length > 39) {
                        GameItem game = new GameItem();
                        game.setGameId(Integer.parseInt(gameData[0]));
                        game.setTitle(gameData[1]);

                        // Release Year parsing from "Feb 20, 2015" or "2015"
                        String releaseDate = gameData[2].replace("\"", "");
                        if (releaseDate.length() >= 4) {
                            try {
                                String yearStr = releaseDate.substring(releaseDate.length() - 4);
                                game.setReleaseYear(Integer.parseInt(yearStr));
                            } catch (NumberFormatException e) {
                                game.setReleaseYear(0);
                            }
                        }

                        game.setReleaseDate(gameData[2]);
                        game.setPrice(gameData[6]);
                        game.setSupportedLanguages(gameData[10]);
                        game.setDeveloper(gameData[33]);
                        game.setPublisher(gameData[34]);

                        game.setDescription(gameData[9]);
                        game.setHeaderImage(gameData[13]);

                        try {
                            // Remove commas from numbers if present (e.g. "1,234")
                            String posStr = gameData[23].replace(",", "").trim();
                            game.setPositiveReviews(Integer.parseInt(posStr));
                        } catch (NumberFormatException e) {
                            game.setPositiveReviews(0);
                        }

                        String genres = gameData[36];
                        if (!genres.trim().isEmpty()) {
                            String[] genreArray = genres.split(",");
                            for (String genre : genreArray) {
                                String cleanGenre = genre.trim();
                                game.addGenre(cleanGenre);
                                addGame2GenreIndex(cleanGenre, game);
                            }
                        }

                        game.setScreenshots(gameData[38]);
                        game.setProductionVideos(gameData[39]);

                        this.gameMap.put(game.getGameId(), game);
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

    // load game embedding
    private void loadGameEmb(String gameEmbPath, String embKey) throws Exception {
        if (Config.EMB_DATA_SOURCE.equals(Config.DATA_SOURCE_FILE)) {
            System.out.println("Loading game embedding from " + gameEmbPath + " ...");
            int validEmbCount = 0;
            try (Scanner scanner = new Scanner(new File(gameEmbPath))) {
                while (scanner.hasNextLine()) {
                    String gameRawEmbData = scanner.nextLine();
                    String[] gameEmbData = gameRawEmbData.split(":");
                    if (gameEmbData.length == 2) {
                        GameItem g = getGameById(Integer.parseInt(gameEmbData[0]));
                        if (null == g) {
                            continue;
                        }
                        g.setEmb(Utility.parseEmbStr(gameEmbData[1]));
                        validEmbCount++;
                    }
                }
            }
            System.out.println("Loading game embedding completed. " + validEmbCount + " game embeddings in total.");
        } else {
            System.out.println("Loading game embedding from Redis ...");
            Set<String> gameEmbKeys = RedisClient.getInstance().keys(embKey + "*");
            int validEmbCount = 0;
            for (String gameEmbKey : gameEmbKeys) {
                String gameId = gameEmbKey.split(":")[1];
                GameItem g = getGameById(Integer.parseInt(gameId));
                if (null == g) {
                    continue;
                }
                g.setEmb(Utility.parseEmbStr(RedisClient.getInstance().get(gameEmbKey)));
                validEmbCount++;
            }
            System.out.println("Loading game embedding completed. " + validEmbCount + " game embeddings in total.");
        }
    }

    // load game features
    private void loadGameFeatures(String gameFeaturesPrefix) throws Exception {
        System.out.println("Loading game features from Redis ...");
        Set<String> gameFeaturesKeys = RedisClient.getInstance().keys(gameFeaturesPrefix + "*");
        int validFeaturesCount = 0;
        for (String gameFeaturesKey : gameFeaturesKeys) {
            String gameId = gameFeaturesKey.split(":")[1];
            GameItem g = getGameById(Integer.parseInt(gameId));
            if (null == g) {
                continue;
            }
            g.setGameFeatures(RedisClient.getInstance().hgetAll(gameFeaturesKey));
            validFeaturesCount++;
        }
        System.out.println("Loading game features completed. " + validFeaturesCount + " game features in total.");
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
                    int gameId = Integer.parseInt(linkData[0]);
                    GameItem game = this.gameMap.get(gameId);
                    if (null != game) {
                        count++;
                        game.setImdbId(linkData[1].trim());
                        game.setTmdbId(linkData[2].trim());
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
                String[] ratingData = ratingRawData.split(",");
                if (ratingData.length == 4) {
                    count++;
                    Rating rating = new Rating();
                    rating.setUserId(Integer.parseInt(ratingData[0]));
                    rating.setGameId(Integer.parseInt(ratingData[1]));
                    rating.setScore(Float.parseFloat(ratingData[2]));
                    rating.setTimestamp(Long.parseLong(ratingData[3]));
                    GameItem game = this.gameMap.get(rating.getGameId());
                    if (null != game) {
                        game.addRating(rating);
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

    // add game to genre reversed index
    private void addGame2GenreIndex(String genre, GameItem game) {
        if (!this.genreReverseIndexMap.containsKey(genre)) {
            this.genreReverseIndexMap.put(genre, new ArrayList<>());
        }
        this.genreReverseIndexMap.get(genre).add(game);
    }

    // get games by genre, and order the games by sortBy method
    public List<GameItem> getGamesByGenre(String genre, int size, String sortBy) {
        if (null != genre) {
            List<GameItem> games = this.genreReverseIndexMap.get(genre);
            if (null == games) {
                return new ArrayList<>();
            }
            games = new ArrayList<>(games);
            switch (sortBy) {
                case "rating":
                    games.sort((g1, g2) -> Double.compare(g2.getAverageRating(), g1.getAverageRating()));
                    break;
                case "releaseYear":
                    games.sort((g1, g2) -> Integer.compare(g2.getReleaseYear(), g1.getReleaseYear()));
                    break;
                case "positiveReviews":
                    games.sort((g1, g2) -> Integer.compare(g2.getPositiveReviews(), g1.getPositiveReviews()));
                    break;
                default:
            }

            if (games.size() > size) {
                return games.subList(0, size);
            }
            return games;
        }
        return null;
    }

    // get top N games order by sortBy method
    public List<GameItem> getGames(int size, String sortBy) {
        List<GameItem> games = new ArrayList<>(gameMap.values());
        switch (sortBy) {
            case "rating":
                games.sort((g1, g2) -> Double.compare(g2.getAverageRating(), g1.getAverageRating()));
                break;
            case "releaseYear":
                games.sort((g1, g2) -> Integer.compare(g2.getReleaseYear(), g1.getReleaseYear()));
                break;
            case "positiveReviews":
                games.sort((g1, g2) -> Integer.compare(g2.getPositiveReviews(), g1.getPositiveReviews()));
                break;
            default:
        }

        if (games.size() > size) {
            return games.subList(0, size);
        }
        return games;
    }

    public List<GameItem> searchGames(String query, int size) {
        List<GameItem> result = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return result;
        }
        String lowerQuery = query.toLowerCase();
        for (GameItem game : gameMap.values()) {
            if (game.getTitle().toLowerCase().contains(lowerQuery)) {
                result.add(game);
            }
        }
        // Sort by popularity (positive reviews)
        result.sort((g1, g2) -> g2.getPositiveReviews() - g1.getPositiveReviews());

        if (result.size() > size) {
            return result.subList(0, size);
        }
        return result;
    }

    // get game object by game id
    public GameItem getGameById(int gameId) {
        return this.gameMap.get(gameId);
    }

    // get user object by user id
    public User getUserById(int userId) {
        return this.userMap.get(userId);
    }
}
