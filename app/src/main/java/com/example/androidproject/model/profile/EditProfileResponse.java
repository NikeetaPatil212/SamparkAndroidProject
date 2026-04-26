package com.example.androidproject.model.profile;


import com.google.gson.annotations.SerializedName;

public class EditProfileResponse {

    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }
}