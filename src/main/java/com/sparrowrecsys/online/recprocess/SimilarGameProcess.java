package com.sparrowrecsys.online.recprocess;

import com.sparrowrecsys.online.datamanager.DataManager;
import com.sparrowrecsys.online.datamanager.GameItem;
import java.util.*;

/**
 * Recommendation process of similar games
 */
public class SimilarGameProcess {

    /**
     * get recommendation game list
     * 
     * @param gameId input game id
     * @param size   size of similar items
     * @param model  model used for calculating similarity
     * @return list of similar games
     */
    public static List<GameItem> getRecList(int gameId, int size, String model) {
        GameItem game = DataManager.getInstance().getGameById(gameId);
        if (null == game) {
            return new ArrayList<>();
        }
        List<GameItem> candidates = candidateGenerator(game);
        List<GameItem> rankedList = ranker(game, candidates, model);

        if (rankedList.size() > size) {
            return rankedList.subList(0, size);
        }
        return rankedList;
    }

    /**
     * generate candidates for similar games recommendation
     * 
     * @param game input game object
     * @return game candidates
     */
    public static List<GameItem> candidateGenerator(GameItem game) {
        HashMap<Integer, GameItem> candidateMap = new HashMap<>();
        for (String genre : game.getGenres()) {
            List<GameItem> oneCandidates = DataManager.getInstance().getGamesByGenre(genre, 100, "rating");
            for (GameItem candidate : oneCandidates) {
                candidateMap.put(candidate.getGameId(), candidate);
            }
        }
        candidateMap.remove(game.getGameId());
        return new ArrayList<>(candidateMap.values());
    }

    /**
     * multiple-retrieval candidate generation method
     * 
     * @param game input game object
     * @return game candidates
     */
    public static List<GameItem> multipleRetrievalCandidates(GameItem game) {
        if (null == game) {
            return null;
        }

        HashSet<String> genres = new HashSet<>(game.getGenres());

        HashMap<Integer, GameItem> candidateMap = new HashMap<>();
        for (String genre : genres) {
            List<GameItem> oneCandidates = DataManager.getInstance().getGamesByGenre(genre, 20, "rating");
            for (GameItem candidate : oneCandidates) {
                candidateMap.put(candidate.getGameId(), candidate);
            }
        }

        List<GameItem> highRatingCandidates = DataManager.getInstance().getGames(100, "rating");
        for (GameItem candidate : highRatingCandidates) {
            candidateMap.put(candidate.getGameId(), candidate);
        }

        List<GameItem> latestCandidates = DataManager.getInstance().getGames(100, "releaseYear");
        for (GameItem candidate : latestCandidates) {
            candidateMap.put(candidate.getGameId(), candidate);
        }

        candidateMap.remove(game.getGameId());
        return new ArrayList<>(candidateMap.values());
    }

    /**
     * embedding based candidate generation method
     * 
     * @param game input game
     * @param size size of candidate pool
     * @return game candidates
     */
    public static List<GameItem> retrievalCandidatesByEmbedding(GameItem game, int size) {
        if (null == game || null == game.getEmb()) {
            return null;
        }

        List<GameItem> allCandidates = DataManager.getInstance().getGames(10000, "rating");
        HashMap<GameItem, Double> gameScoreMap = new HashMap<>();
        for (GameItem candidate : allCandidates) {
            double similarity = calculateEmbSimilarScore(game, candidate);
            gameScoreMap.put(candidate, similarity);
        }

        List<Map.Entry<GameItem, Double>> gameScoreList = new ArrayList<>(gameScoreMap.entrySet());
        gameScoreList.sort(Map.Entry.comparingByValue());

        List<GameItem> candidates = new ArrayList<>();
        for (Map.Entry<GameItem, Double> gameScoreEntry : gameScoreList) {
            candidates.add(gameScoreEntry.getKey());
        }

        return candidates.subList(0, Math.min(candidates.size(), size));
    }

    /**
     * rank candidates
     * 
     * @param game       input game
     * @param candidates game candidates
     * @param model      model name used for ranking
     * @return ranked game list
     */
    public static List<GameItem> ranker(GameItem game, List<GameItem> candidates, String model) {
        HashMap<GameItem, Double> candidateScoreMap = new HashMap<>();
        for (GameItem candidate : candidates) {
            double similarity;
            switch (model) {
                case "emb":
                    similarity = calculateEmbSimilarScore(game, candidate);
                    break;
                default:
                    similarity = calculateSimilarScore(game, candidate);
            }
            candidateScoreMap.put(candidate, similarity);
        }
        List<GameItem> rankedList = new ArrayList<>();
        candidateScoreMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(g -> rankedList.add(g.getKey()));
        return rankedList;
    }

    /**
     * function to calculate similarity score
     * 
     * @param game      input game
     * @param candidate candidate game
     * @return similarity score
     */
    public static double calculateSimilarScore(GameItem game, GameItem candidate) {
        int sameGenreCount = 0;
        for (String genre : game.getGenres()) {
            if (candidate.getGenres().contains(genre)) {
                sameGenreCount++;
            }
        }
        double genreSimilarity = (double) sameGenreCount / (game.getGenres().size() + candidate.getGenres().size()) / 2;
        double ratingScore = candidate.getAverageRating() / 5;
        double similarityWeight = 0.7;
        double ratingScoreWeight = 0.3;
        return genreSimilarity * similarityWeight + ratingScore * ratingScoreWeight;
    }

    /**
     * function to calculate similarity score based on embedding
     *
     * @param game      input game
     * @param candidate candidate game
     * @return similarity score
     */
    public static double calculateEmbSimilarScore(GameItem game, GameItem candidate) {
        if (null == game || null == candidate) {
            return -1;
        }
        if (null == game.getEmb() || null == candidate.getEmb()) {
            return -1;
        }
        return game.getEmb().calculateSimilarity(candidate.getEmb());
    }
}