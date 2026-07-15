package com.example.androidproject.model;


import com.google.gson.annotations.SerializedName;

public class CancelAdmissionResponse {

    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("message")
    private String message;

    // Empty Constructor
    public CancelAdmissionResponse() {
    }

    // Getters and Setters

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}