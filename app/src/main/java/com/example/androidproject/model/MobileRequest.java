package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;

public class MobileRequest {

    @SerializedName("mobile")
    private String mobile;

    public MobileRequest(String mobile) {
        this.mobile = mobile;
    }
}
