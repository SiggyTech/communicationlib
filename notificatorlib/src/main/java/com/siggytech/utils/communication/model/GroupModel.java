package com.siggytech.utils.communication.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GroupModel {
    @SerializedName("idgroup")
    public long idGroup;
    @SerializedName("groupname")
    public String name;
    public List<UserModel> usersConnected;
    public List<UserModel> totalUsers;

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
