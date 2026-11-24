package com.optic.socialmediagamer.models;

public class User {

    private String id;
    private String email;
    private String username;
    private String phone;
    private String imageProfile;
    private String imageCover;
    private long timestamp;
    private long lastConnection;
    private boolean online;
    private String bio;
    private String favoriteGame;
    private String psnId;
    private String xboxTag;
    private String discordTag;

    public User() {

    }

    public User(String id, String email, String username, String phone, String imageProfile, String imageCover, long timestamp, long lastConnection, boolean online) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.phone = phone;
        this.imageProfile = imageProfile;
        this.imageCover = imageCover;
        this.timestamp = timestamp;
        this.lastConnection = lastConnection;
        this.online = online;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageProfile() {
        return imageProfile;
    }

    public void setImageProfile(String imageProfile) {
        this.imageProfile = imageProfile;
    }

    public String getImageCover() {
        return imageCover;
    }

    public void setImageCover(String imageCover) {
        this.imageCover = imageCover;
    }

    public long getLastConnection() {
        return lastConnection;
    }

    public void setLastConnection(long lastConnection) {
        this.lastConnection = lastConnection;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getFavoriteGame() {
        return favoriteGame;
    }

    public void setFavoriteGame(String favoriteGame) {
        this.favoriteGame = favoriteGame;
    }

    public String getPsnId() {
        return psnId;
    }

    public void setPsnId(String psnId) {
        this.psnId = psnId;
    }

    public String getXboxTag() {
        return xboxTag;
    }

    public void setXboxTag(String xboxTag) {
        this.xboxTag = xboxTag;
    }

    public String getDiscordTag() {
        return discordTag;
    }

    public void setDiscordTag(String discordTag) {
        this.discordTag = discordTag;
    }
}
