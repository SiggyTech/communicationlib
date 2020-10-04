package com.siggytech.utils.communication;

public class ChatModel {

    private Long id;

    private MessageModel messageModel;
    private String from;
    private String dateTime;
    private boolean isMine;

    public ChatModel() {
    }

    public ChatModel(Long id, MessageModel messageModel, String from, String dateTime, boolean isMine) {
        this.id = id;
        this.messageModel = messageModel;
        this.from = from;
        this.dateTime = dateTime;
        this.isMine = isMine;
    }

    public Long getIdMessage() {
        return this.id;
    }

    public String getFromMessage() {
        return this.from;
    }
    public String getDateTimeMessage() {
        return this.dateTime;
    }
    public MessageModel getMessageModel() {
        return messageModel;
    }
    public boolean isMine() {
        return isMine;
    }
}