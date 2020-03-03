package com.siggytech.utils.communication;

public class ChatModel {

    private Long id;

    private String textMessage;
    private String from;
    private String dateTime;
    private String messageType;

    public ChatModel(Long id, String textMessage, String from, String dateTime, String type) {
        this.id = id;
        this.textMessage = textMessage;
        this.from = from;
        this.dateTime = dateTime;
        this.messageType = type;

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
    public String getMessageType() {
        return messageType;
    }

}