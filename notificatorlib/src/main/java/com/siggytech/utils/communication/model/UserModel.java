package com.siggytech.utils.communication.model;

import com.google.gson.annotations.SerializedName;

public class UserModel {
    @SerializedName("imei")
    public String deviceToken;
    @SerializedName("clientname")
    public String clientName;
    public String username;

}
