package com.example.androidproject.model.template;

import com.google.gson.annotations.SerializedName;

public class TemplateRequest {

    @SerializedName("userID")
    private int userID;

    @SerializedName("instituteID")
    private int instituteID;

    public TemplateRequest(int userID, int instituteID) {
        this.userID      = userID;
        this.instituteID = instituteID;
    }
}