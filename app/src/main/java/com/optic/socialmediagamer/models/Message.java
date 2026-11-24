package com.optic.socialmediagamer.models;

public class Message {

    private String id;
    private String idSender;
    private String idReceiver;
    private String idChat;
    private String message;
    private long timestamp;
    private boolean viewed;
    private String type; // "text", "image", "audio"
    private String url;
    private String duration;
    private String reaction;
    private String challengeGame;
    private String challengeStatus; // "pending", "accepted", "declined", "finished"
    private String challengeWinner;
    private String challengeStake;

    public Message() {

    }

    public Message(String id, String idSender, String idReceiver, String idChat, String message, long timestamp, boolean viewed) {
        this.id = id;
        this.idSender = idSender;
        this.idReceiver = idReceiver;
        this.idChat = idChat;
        this.message = message;
        this.timestamp = timestamp;
        this.viewed = viewed;
        this.type = "text";
        this.url = "";
    }

    public Message(String id, String idSender, String idReceiver, String idChat, String message, long timestamp, boolean viewed, String type, String url) {
        this.id = id;
        this.idSender = idSender;
        this.idReceiver = idReceiver;
        this.idChat = idChat;
        this.message = message;
        this.timestamp = timestamp;
        this.viewed = viewed;
        this.type = type;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdSender() {
        return idSender;
    }

    public void setIdSender(String idSender) {
        this.idSender = idSender;
    }

    public String getIdReceiver() {
        return idReceiver;
    }

    public void setIdReceiver(String idReceiver) {
        this.idReceiver = idReceiver;
    }

    public String getIdChat() {
        return idChat;
    }

    public void setIdChat(String idChat) {
        this.idChat = idChat;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getReaction() {
        return reaction;
    }

    public void setReaction(String reaction) {
        this.reaction = reaction;
    }

    public String getChallengeGame() {
        return challengeGame;
    }

    public void setChallengeGame(String challengeGame) {
        this.challengeGame = challengeGame;
    }

    public String getChallengeStatus() {
        return challengeStatus;
    }

    public void setChallengeStatus(String challengeStatus) {
        this.challengeStatus = challengeStatus;
    }

    public String getChallengeWinner() {
        return challengeWinner;
    }

    public void setChallengeWinner(String challengeWinner) {
        this.challengeWinner = challengeWinner;
    }

    public String getChallengeStake() {
        return challengeStake;
    }

    public void setChallengeStake(String challengeStake) {
        this.challengeStake = challengeStake;
    }
}
