package com.sparrowrecsys.online.model;

public class Game {
    private int appId;
    private String name;
    private String releaseDate;
    private String estimatedOwners;
    private int peakCCU;
    private int requiredAge;
    private float price;
    private int discount;
    private int dlcCount;
    private String aboutTheGame;
    private String supportedLanguages;
    private String fullAudioLanguages;
    private String reviews;
    private String headerImage;
    private String website;
    private String supportUrl;
    private String supportEmail;
    private boolean windows;
    private boolean mac;
    private boolean linux;
    private int metacriticScore;
    private String metacriticUrl;
    private int userScore;
    private int positive;
    private int negative;
    private String scoreRank;
    private int achievements;
    private int recommendations;
    private String notes;
    private int averagePlaytimeForever;
    private int averagePlaytimeTwoWeeks;
    private int medianPlaytimeForever;
    private int medianPlaytimeTwoWeeks;
    private String developers;
    private String publishers;
    private String categories;
    private String genres;
    private String tags;
    private String screenshots;
    private String movies;

    // Getters and Setters
    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getEstimatedOwners() {
        return estimatedOwners;
    }

    public void setEstimatedOwners(String estimatedOwners) {
        this.estimatedOwners = estimatedOwners;
    }

    public int getPeakCCU() {
        return peakCCU;
    }

    public void setPeakCCU(int peakCCU) {
        this.peakCCU = peakCCU;
    }

    public int getRequiredAge() {
        return requiredAge;
    }

    public void setRequiredAge(int requiredAge) {
        this.requiredAge = requiredAge;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public int getDlcCount() {
        return dlcCount;
    }

    public void setDlcCount(int dlcCount) {
        this.dlcCount = dlcCount;
    }

    public String getAboutTheGame() {
        return aboutTheGame;
    }

    public void setAboutTheGame(String aboutTheGame) {
        this.aboutTheGame = aboutTheGame;
    }

    public String getSupportedLanguages() {
        return supportedLanguages;
    }

    public void setSupportedLanguages(String supportedLanguages) {
        this.supportedLanguages = supportedLanguages;
    }

    public String getFullAudioLanguages() {
        return fullAudioLanguages;
    }

    public void setFullAudioLanguages(String fullAudioLanguages) {
        this.fullAudioLanguages = fullAudioLanguages;
    }

    public String getReviews() {
        return reviews;
    }

    public void setReviews(String reviews) {
        this.reviews = reviews;
    }

    public String getHeaderImage() {
        return headerImage;
    }

    public void setHeaderImage(String headerImage) {
        this.headerImage = headerImage;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getSupportUrl() {
        return supportUrl;
    }

    public void setSupportUrl(String supportUrl) {
        this.supportUrl = supportUrl;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }

    public boolean isWindows() {
        return windows;
    }

    public void setWindows(boolean windows) {
        this.windows = windows;
    }

    public boolean isMac() {
        return mac;
    }

    public void setMac(boolean mac) {
        this.mac = mac;
    }

    public boolean isLinux() {
        return linux;
    }

    public void setLinux(boolean linux) {
        this.linux = linux;
    }

    public int getMetacriticScore() {
        return metacriticScore;
    }

    public void setMetacriticScore(int metacriticScore) {
        this.metacriticScore = metacriticScore;
    }

    public String getMetacriticUrl() {
        return metacriticUrl;
    }

    public void setMetacriticUrl(String metacriticUrl) {
        this.metacriticUrl = metacriticUrl;
    }

    public int getUserScore() {
        return userScore;
    }

    public void setUserScore(int userScore) {
        this.userScore = userScore;
    }

    public int getPositive() {
        return positive;
    }

    public void setPositive(int positive) {
        this.positive = positive;
    }

    public int getNegative() {
        return negative;
    }

    public void setNegative(int negative) {
        this.negative = negative;
    }

    public String getScoreRank() {
        return scoreRank;
    }

    public void setScoreRank(String scoreRank) {
        this.scoreRank = scoreRank;
    }

    public int getAchievements() {
        return achievements;
    }

    public void setAchievements(int achievements) {
        this.achievements = achievements;
    }

    public int getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(int recommendations) {
        this.recommendations = recommendations;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getAveragePlaytimeForever() {
        return averagePlaytimeForever;
    }

    public void setAveragePlaytimeForever(int averagePlaytimeForever) {
        this.averagePlaytimeForever = averagePlaytimeForever;
    }

    public int getAveragePlaytimeTwoWeeks() {
        return averagePlaytimeTwoWeeks;
    }

    public void setAveragePlaytimeTwoWeeks(int averagePlaytimeTwoWeeks) {
        this.averagePlaytimeTwoWeeks = averagePlaytimeTwoWeeks;
    }

    public int getMedianPlaytimeForever() {
        return medianPlaytimeForever;
    }

    public void setMedianPlaytimeForever(int medianPlaytimeForever) {
        this.medianPlaytimeForever = medianPlaytimeForever;
    }

    public int getMedianPlaytimeTwoWeeks() {
        return medianPlaytimeTwoWeeks;
    }

    public void setMedianPlaytimeTwoWeeks(int medianPlaytimeTwoWeeks) {
        this.medianPlaytimeTwoWeeks = medianPlaytimeTwoWeeks;
    }

    public String getDevelopers() {
        return developers;
    }

    public void setDevelopers(String developers) {
        this.developers = developers;
    }

    public String getPublishers() {
        return publishers;
    }

    public void setPublishers(String publishers) {
        this.publishers = publishers;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(String screenshots) {
        this.screenshots = screenshots;
    }

    public String getMovies() {
        return movies;
    }

    public void setMovies(String movies) {
        this.movies = movies;
    }
}
