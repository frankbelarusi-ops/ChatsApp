package com.optic.socialmediagamer.models;

public class LFGPost {

    private String id;
    private String idUser;
    private String username;
    private String userPhoto;
    private String game;
    private String platform;
    private String rank;
    private String description;
    private boolean micRequired;
    private long timestamp;

    public LFGPost() {
    }

    public LFGPost(String id, String idUser, String username, String userPhoto, String game, String platform, String rank, String description, boolean micRequired, long timestamp) {
        this.id = id;
        this.idUser = idUser;
        this.username = username;
        this.userPhoto = userPhoto;
        this.game = game;
        this.platform = platform;
        this.rank = rank;
        this.description = description;
        this.micRequired = micRequired;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isMicRequired() {
        return micRequired;
    }

    public void setMicRequired(boolean micRequired) {
        this.micRequired = micRequired;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
