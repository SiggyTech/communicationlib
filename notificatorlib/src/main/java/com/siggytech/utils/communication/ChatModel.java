package com.siggytech.utils.communication;

public class ChatModel {

    private Long id;

    private String textMessage;

    private String from;
    private String dateTime;


    public ChatModel(Long id, String textMessage, String from, String dateTime) {
        this.id = id;
        this.textMessage = textMessage;
        this.from = from;
        this.dateTime = dateTime;
    }
    public Long getIdMessage() {
        return this.id;
    }
    public String getTextMessage() {
        return this.textMessage;
    }
    public String getFromMessage() {
        return this.from;
    }
    public String getDateTimeMessage() {
        return this.dateTime;
    }



}