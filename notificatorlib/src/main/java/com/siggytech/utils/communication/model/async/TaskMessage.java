package com.siggytech.utils.communication.model.async;

import com.siggytech.utils.communication.model.MessageModel;

public class TaskMessage {
    private String message;
    private Exception exception;
    private boolean error;
    private boolean isAlerta;
    private MessageModel messageModel;
    private ApiEnum apiEnum;

    public TaskMessage() {
    }

    public TaskMessage(boolean error) {
        this.error = error;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public boolean isAlerta() {
        return isAlerta;
    }

    public void setAlerta(boolean alerta) {
        isAlerta = alerta;
    }

    public MessageModel getMessageModel() {
        return messageModel;
    }

    public void setMessageModel(MessageModel messageModel) {
        this.messageModel = messageModel;
    }

    public ApiEnum getApiEnum() {
        return apiEnum;
    }

    public void setApiEnum(ApiEnum apiEnum) {
        this.apiEnum = apiEnum;
    }
}
