package com.sparrowrecsys.online.recprocess;

import com.sparrowrecsys.online.datamanager.DataManager;
import com.sparrowrecsys.online.datamanager.User;
import com.sparrowrecsys.online.datamanager.GameItem;
import com.sparrowrecsys.online.datamanager.RedisClient;
import com.sparrowrecsys.online.util.Config;
import com.sparrowrecsys.online.util.Utility;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static com.sparrowrecsys.online.util.HttpClient.asyncSinglePostRequest;

/**
 * Recommendation process of similar games
 */

public class RecForYouProcess {

    /**
     * get recommendation game list
     * 
     * @param userId input user id
     * @param size   size of similar items
     * @param model  model used for calculating similarity
     * @return list of similar games
     */
    public static List<GameItem> getRecList(int userId, int size, String model) {
        User user = DataManager.getInstance().getUserById(userId);
        if (null == user) {
            return new ArrayList<>();
        }
        final int CANDIDATE_SIZE = 800;
        List<GameItem> candidates = DataManager.getInstance().getGames(CANDIDATE_SIZE, "rating");

        // load user emb from redis if data source is redis
        if (Config.EMB_DATA_SOURCE.equals(Config.DATA_SOURCE_REDIS)) {
            String userEmbKey = "uEmb:" + userId;
            String userEmb = RedisClient.getInstance().get(userEmbKey);
            if (null != userEmb) {
                user.setEmb(Utility.parseEmbStr(userEmb));
            }
        }

        if (Config.IS_LOAD_USER_FEATURE_FROM_REDIS) {
            String userFeaturesKey = "uf:" + userId;
            Map<String, String> userFeatures = RedisClient.getInstance().hgetAll(userFeaturesKey);
            if (null != userFeatures) {
                user.setUserFeatures(userFeatures);
            }
        }

        List<GameItem> rankedList = ranker(user, candidates, model);

        if (rankedList.size() > size) {
            return rankedList.subList(0, size);
        }
        return rankedList;
    }

    /**
     * rank candidates
     *
     * @param user       input user
     * @param candidates game candidates
     * @param model      model name used for ranking
     * @return ranked game list
     */
    public static List<GameItem> ranker(User user, List<GameItem> candidates, String model) {
        HashMap<GameItem, Double> candidateScoreMap = new HashMap<>();

        switch (model) {
            case "emb":
                for (GameItem candidate : candidates) {
                    double similarity = calculateEmbSimilarScore(user, candidate);
                    candidateScoreMap.put(candidate, similarity);
                }
                break;
            case "nerualcf":
                callNeuralCFTFServing(user, candidates, candidateScoreMap);
                break;
            default:
                // default ranking in candidate set
                for (int i = 0; i < candidates.size(); i++) {
                    candidateScoreMap.put(candidates.get(i), (double) (candidates.size() - i));
                }
        }

        List<GameItem> rankedList = new ArrayList<>();
        candidateScoreMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(g -> rankedList.add(g.getKey()));
        return rankedList;
    }

    /**
     * function to calculate similarity score based on embedding
     *
     * @param user      input user
     * @param candidate candidate game
     * @return similarity score
     */
    public static double calculateEmbSimilarScore(User user, GameItem candidate) {
        if (null == user || null == candidate || null == user.getEmb()) {
            return -1;
        }
        return user.getEmb().calculateSimilarity(candidate.getEmb());
    }

    /**
     * call TenserFlow serving to get the NeuralCF model inference result
     *
     * @param user              input user
     * @param candidates        candidate games
     * @param candidateScoreMap save prediction score into the score map
     */
    public static void callNeuralCFTFServing(User user, List<GameItem> candidates,
            HashMap<GameItem, Double> candidateScoreMap) {
        if (null == user || null == candidates || candidates.size() == 0) {
            return;
        }

        JSONArray instances = new JSONArray();
        for (GameItem g : candidates) {
            JSONObject instance = new JSONObject();
            instance.put("userId", user.getUserId());
            instance.put("gameId", g.getGameId());
            instances.put(instance);
        }

        JSONObject instancesRoot = new JSONObject();
        instancesRoot.put("instances", instances);

        // need to confirm the tf serving end point
        String predictionScores = asyncSinglePostRequest("http://localhost:8501/v1/models/recmodel:predict",
                instancesRoot.toString());
        System.out.println("send user" + user.getUserId() + " request to tf serving.");

        JSONObject predictionsObject = new JSONObject(predictionScores);
        JSONArray scores = predictionsObject.getJSONArray("predictions");
        for (int i = 0; i < candidates.size(); i++) {
            candidateScoreMap.put(candidates.get(i), scores.getJSONArray(i).getDouble(0));
        }
    }
}
