package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;

public class UserDetails {

    @SerializedName("userRole")
    private String userRole;

    @SerializedName("operatorID")
    private String operatorID;

    @SerializedName("userName")
    private String userName;

    public String getUserRole() {
        return userRole;
    }

    public String getOperatorID() {
        return operatorID;
    }

    public String getUserName() {
        return userName;
    }
}

