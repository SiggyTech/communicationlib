package com.siggytech.utils.communication;

public class Group {
    public long idGroup;
    public String name;

    public Group(long idGroup, String name){
        this.idGroup = idGroup;
        this.name = name;
    }

    @Override
    public String toString() {
        return "idGroup=" + idGroup;
    }
}
