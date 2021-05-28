package com.siggytech.utils.communication.model;

import com.google.gson.annotations.SerializedName;

public class UserModel {
    @SerializedName("token")
    public String deviceToken;
    public String username;

}
