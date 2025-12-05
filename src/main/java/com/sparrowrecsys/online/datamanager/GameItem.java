package com.sparrowrecsys.online.datamanager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sparrowrecsys.online.model.Embedding;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * GameItem Class, contains attributes loaded from games CSV and other advanced
 * data like averageRating, emb, etc.
 */
public class GameItem {
    int gameId;
    String title;
    int releaseYear;
    String imdbId;
    String tmdbId;
    List<String> genres;
    // how many user rate the game
    int ratingNumber;
    // average rating score
    double averageRating;

    // Fields for Game data
    String description;
    String headerImage;
    String screenshots; // Comma separated URLs
    String productionVideos; // Comma separated URLs
    int positiveReviews;
    String releaseDate;
    String publisher;
    String developer;
    String price;
    String supportedLanguages;

    // embedding of the game
    @JsonIgnore
    Embedding emb;

    // all rating scores list
    @JsonIgnore
    List<Rating> ratings;

    @JsonIgnore
    Map<String, String> gameFeatures;

    final int TOP_RATING_SIZE = 10;

    @JsonSerialize(using = RatingListSerializer.class)
    List<Rating> topRatings;

    public GameItem() {
        ratingNumber = 0;
        averageRating = 0;
        this.genres = new ArrayList<>();
        this.ratings = new ArrayList<>();
        this.topRatings = new LinkedList<>();
        this.emb = null;
        this.gameFeatures = null;
        this.description = "";
        this.headerImage = "";
        this.screenshots = "";
        this.productionVideos = "";
        this.positiveReviews = 0;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    // Keep movieId getter/setter for JSON compatibility
    public int getMovieId() {
        return gameId;
    }

    public void setMovieId(int movieId) {
        this.gameId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void addGenre(String genre) {
        this.genres.add(genre);
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public List<Rating> getRatings() {
        return ratings;
    }

    public void addRating(Rating rating) {
        averageRating = (averageRating * ratingNumber + rating.getScore()) / (ratingNumber + 1);
        ratingNumber++;
        this.ratings.add(rating);
        addTopRating(rating);
    }

    public void addTopRating(Rating rating) {
        if (this.topRatings.isEmpty()) {
            this.topRatings.add(rating);
        } else {
            int index = 0;
            for (Rating topRating : this.topRatings) {
                if (topRating.getScore() >= rating.getScore()) {
                    break;
                }
                index++;
            }
            topRatings.add(index, rating);
            if (topRatings.size() > TOP_RATING_SIZE) {
                topRatings.remove(0);
            }
        }
    }

    public String getImdbId() {
        return imdbId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }

    public String getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(String tmdbId) {
        this.tmdbId = tmdbId;
    }

    public int getRatingNumber() {
        return ratingNumber;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public Embedding getEmb() {
        return emb;
    }

    public void setEmb(Embedding emb) {
        this.emb = emb;
    }

    public Map<String, String> getGameFeatures() {
        return gameFeatures;
    }

    public void setGameFeatures(Map<String, String> gameFeatures) {
        this.gameFeatures = gameFeatures;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHeaderImage() {
        return headerImage;
    }

    public void setHeaderImage(String headerImage) {
        this.headerImage = headerImage;
    }

    public String getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(String screenshots) {
        this.screenshots = screenshots;
    }

    public String getProductionVideos() {
        return productionVideos;
    }

    public void setProductionVideos(String productionVideos) {
        this.productionVideos = productionVideos;
    }

    public int getPositiveReviews() {
        return positiveReviews;
    }

    public void setPositiveReviews(int positiveReviews) {
        this.positiveReviews = positiveReviews;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getSupportedLanguages() {
        return supportedLanguages;
    }

    public void setSupportedLanguages(String supportedLanguages) {
        this.supportedLanguages = supportedLanguages;
    }
}
