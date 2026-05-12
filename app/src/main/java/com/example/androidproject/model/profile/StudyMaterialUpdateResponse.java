package com.example.androidproject.model.profile;


import com.google.gson.annotations.SerializedName;

public class StudyMaterialUpdateResponse {
    @SerializedName("isSuccess") private boolean success;
    @SerializedName("message")   private String message;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}