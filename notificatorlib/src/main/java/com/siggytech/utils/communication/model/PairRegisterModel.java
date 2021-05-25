package com.siggytech.utils.communication.model;

public class PairRegisterModel {

    private String tokenDevice;
    private String tokenFirebase;

    public PairRegisterModel(String tokenDevice, String tokenFirebase) {
        this.tokenDevice = tokenDevice;
        this.tokenFirebase = tokenFirebase;
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
}
