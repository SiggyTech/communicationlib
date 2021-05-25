package com.siggytech.utils.communication.model;

import com.google.gson.annotations.SerializedName;

public class GroupRequestModel {
    @SerializedName("imei")
    private String deviceToken;
    @SerializedName("API_KEY")
    private String apiKey;

    public GroupRequestModel() {
    }

    public GroupRequestModel(String deviceToken, String apiKey) {
        this.deviceToken = deviceToken;
        this.apiKey = apiKey;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
