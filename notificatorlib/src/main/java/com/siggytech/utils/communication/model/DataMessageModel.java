package com.siggytech.utils.communication.model;

import com.google.gson.annotations.SerializedName;

public class DataMessageModel {

    private String from;
    private String idGroupFrom;
    private String text;
    private String dateTime;
    @SerializedName("msgpart")
    private String msgPart;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getIdGroupFrom() {
        return idGroupFrom;
    }

    public void setIdGroupFrom(String idGroupFrom) {
        this.idGroupFrom = idGroupFrom;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getMsgPart() {
        return msgPart;
    }

    public void setMsgPart(String msgPart) {
        this.msgPart = msgPart;
    }
}
