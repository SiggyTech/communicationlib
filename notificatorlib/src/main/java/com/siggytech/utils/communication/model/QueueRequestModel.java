package com.siggytech.utils.communication.model;

import com.google.gson.annotations.SerializedName;

public class QueueRequestModel {

    @SerializedName("iddevice")
    private String idDevice;
    @SerializedName("API_KEY")
    private String apiKey;
    @SerializedName("groupid")
    private String idGroup;
    @SerializedName("timemark")
    private String timeMark;


    public QueueRequestModel(String idDevice, String apiKey, String idGroup, String timeMark) {
        this.idDevice = idDevice;
        this.apiKey = apiKey;
        this.idGroup = idGroup;
        this.timeMark = timeMark;
    }

    public String getIdDevice() {
        return idDevice;
    }

    public void setIdDevice(String idDevice) {
        this.idDevice = idDevice;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(String idGroup) {
        this.idGroup = idGroup;
    }

    public String getTimeMark() {
        return timeMark;
    }

    public void setTimeMark(String timeMark) {
        this.timeMark = timeMark;
    }
}
