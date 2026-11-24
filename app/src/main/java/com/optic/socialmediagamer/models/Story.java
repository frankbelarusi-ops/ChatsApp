package com.optic.socialmediagamer.models;

public class Story {

    private String id;
    private String idUser;
    private String username;
    private String userPhoto;
    private String imageUrl;
    private String caption;
    private long timestamp;
    private long expireTimestamp;

    public Story() {
    }

    public Story(String id, String idUser, String username, String userPhoto, String imageUrl, String caption, long timestamp, long expireTimestamp) {
        this.id = id;
        this.idUser = idUser;
        this.username = username;
        this.userPhoto = userPhoto;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.timestamp = timestamp;
        this.expireTimestamp = expireTimestamp;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getExpireTimestamp() {
        return expireTimestamp;
    }

    public void setExpireTimestamp(long expireTimestamp) {
        this.expireTimestamp = expireTimestamp;
    }
}
