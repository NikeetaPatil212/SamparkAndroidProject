package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("userDetails")
    private UserDetails userDetails;

    public String getMessage() {
        return message;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }
}

