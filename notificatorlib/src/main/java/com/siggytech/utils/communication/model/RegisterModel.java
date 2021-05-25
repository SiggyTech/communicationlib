package com.siggytech.utils.communication.model;

import com.google.gson.annotations.SerializedName;

public class RegisterModel {

    @SerializedName("usertoken")
    private String userToken;
    private String name;
    private int group;

    public RegisterModel() {
    }

    public RegisterModel(String userToken, String name) {
        this.userToken = userToken;
        this.name = name;
        this.group = 1;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }
}
