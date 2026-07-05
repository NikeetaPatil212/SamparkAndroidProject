package com.example.androidproject.model.course;

import com.google.gson.annotations.SerializedName;

public class BatchTimeListRequest {

    @SerializedName("userID")
    public int userID;

    @SerializedName("instituteID")
    public int instituteID;

    public BatchTimeListRequest(int userID, int instituteID) {
        this.userID = userID;
        this.instituteID = instituteID;
    }
}
