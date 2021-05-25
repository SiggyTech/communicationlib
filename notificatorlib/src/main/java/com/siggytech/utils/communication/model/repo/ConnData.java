package com.siggytech.utils.communication.model.repo;

public class ConnData {
    private long id;
    private int idState;
    private int idService;
    private long dateTime;

    public ConnData() {
    }

    public ConnData(int idState, int idService, long dateTime) {
        this.idState = idState;
        this.idService = idService;
        this.dateTime = dateTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getIdState() {
        return idState;
    }

    public void setIdState(int idState) {
        this.idState = idState;
    }

    public int getIdService() {
        return idService;
    }

    public void setIdService(int idService) {
        this.idService = idService;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }
}
