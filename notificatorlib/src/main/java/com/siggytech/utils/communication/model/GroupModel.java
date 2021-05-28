package com.siggytech.utils.communication.model;

import com.google.gson.annotations.SerializedName;

public class GroupModel {
    @SerializedName("idgroup")
    public long idGroup;
    @SerializedName("groupname")
    public String name;
    public UserModel usersConnected;
    public UserModel totalUsers;

    public GroupModel() {
    }

    public GroupModel(long idGroup, String name){
        this.idGroup = idGroup;
        this.name = name;
    }

    @Override
    public String toString() {
        return "idGroup=" + idGroup;
    }
}
