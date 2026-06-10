package com.example.androidproject.model.template;

import com.google.gson.annotations.SerializedName;

public class InstituteRequest {
    @SerializedName("userID")      public int userID;
    @SerializedName("instituteID") public int instituteID;

    public InstituteRequest(int userID, int instituteID) {
        this.userID      = userID;
        this.instituteID = instituteID;
    }
}