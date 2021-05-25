package com.siggytech.utils.communication.model.repo;

public class MessageRaw {

    private long id;
    private String userKey;
    private String idGroup;
    private String from;
    private String message;
    private int mine;
    private long timeMark;
    private int send;

    public MessageRaw() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(String idGroup) {
        this.idGroup = idGroup;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getMine() {
        return mine;
    }

    public void setMine(int mine) {
        this.mine = mine;
    }

    public long getTimeMark() {
        return timeMark;
    }

    public void setTimeMark(long timeMark) {
        this.timeMark = timeMark;
    }

    public int getSend() {
        return send;
    }

    public void setSend(int send) {
        this.send = send;
    }
}
