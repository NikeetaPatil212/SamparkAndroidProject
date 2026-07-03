package com.example.androidproject.model;


import com.google.gson.annotations.SerializedName;

public class AbortInquiryResponse {
    @SerializedName("isSuccess")
    public boolean isSuccess;

    @SerializedName("message")
    public String message;
}