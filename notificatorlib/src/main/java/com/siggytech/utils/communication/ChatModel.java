package com.siggytech.utils.communication;

public class ChatModel {

    private Long id;

    private String textMessage;

    private String from;


    public ChatModel(Long id, String textMessage, String from) {
        this.id = id;
        this.textMessage = textMessage;
        this.from = from;
    }
    public String getMessage() {
        return this.textMessage;
    }



}