package com.example.androidproject.model;


import com.google.gson.annotations.SerializedName;

public class AddInstituteResponse {

    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("message")
    private String message;

    @SerializedName("instituteID")
    private int instituteID;

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public int getInstituteID() {
        return instituteID;
    }
}