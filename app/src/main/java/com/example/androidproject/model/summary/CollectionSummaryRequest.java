package com.example.androidproject.model.summary;


import com.google.gson.annotations.SerializedName;

public class CollectionSummaryRequest {

    @SerializedName("userID")
    private int userID;

    @SerializedName("instituteID")
    private int instituteID;

    public CollectionSummaryRequest(int userID, int instituteID) {
        this.userID      = userID;
        this.instituteID = instituteID;
    }
}