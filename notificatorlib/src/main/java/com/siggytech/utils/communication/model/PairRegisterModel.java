package com.siggytech.utils.communication.model;

public class PairRegisterModel {

    private String tokenDevice;
    private String tokenFirebase;
    private String userName;

    public PairRegisterModel(String tokenDevice, String tokenFirebase) {
        this.tokenDevice = tokenDevice;
        this.tokenFirebase = tokenFirebase;
    }

    public PairRegisterModel(String tokenDevice, String tokenFirebase, String userName) {
        this.tokenDevice = tokenDevice;
        this.tokenFirebase = tokenFirebase;
        this.userName = userName;
    }

    public String getTokenDevice() {
        return tokenDevice;
    }

    public void setTokenDevice(String tokenDevice) {
        this.tokenDevice = tokenDevice;
    }

    public String getTokenFirebase() {
        return tokenFirebase;
    }

    public void setTokenFirebase(String tokenFirebase) {
        this.tokenFirebase = tokenFirebase;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
